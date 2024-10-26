package task4;
import task3.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class MessageQueue {
    Listener listener;
    Channel channel;

    List<Message> queue;
    boolean sending = false;

    class Message {
        byte[] bytes;
        int offset;
        int length;
        Message(byte[] bytes, int offset, int length){
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        };
    }

    /**
     * Convertit un entier en tableau d'octets.
     * @param value L'entier à convertir.
     * @return Un tableau de 4 octets représentant l'entier.
     */
    private static byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value
        };
    }

    /**
     * Convertit un tableau de 4 octets en entier.
     * @param bytes Le tableau de 4 octets à convertir.
     * @return L'entier correspondant au tableau d'octets.
     */
    private static int byteArrayToInt(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Le tableau doit contenir exactement 4 octets");
        }
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }

    class Sender implements Runnable{
        final Channel channel;
        final Listener listener;
        final ChannelWriteListener channelListener;
        byte[] messageBytes;
        int written = 0;
        int messageLength;
        byte[] messageLengthByte;
        Sender parentSender = this;

        class ChannelWriteListener implements Channel.WriteListener{
            @Override
            public void written(int written){
                parentSender.written += written;
                EventPump.getInstance().post(parentSender);
            }
            @Override
            public void disconnected(){
                listener.closed();
            }
        }

        Sender(Channel channel, Listener listener, byte[] bytes, int offset, int length){
            this.channel = channel;
            this.messageBytes = bytes;
            this.messageLength = length;
            this.listener = listener;
            this.messageLengthByte = intToByteArray(messageLength);
            this.channelListener = new ChannelWriteListener();
        }

        @Override
        public void run(){
            if(written < 4){
                // System.out.println(written + "<4");
                channel.write(messageLengthByte, written, 4 - written, channelListener);
            } else {
                if(written - 4 < messageLength){
                    // System.out.println(written + "< messageLength");
                    channel.write(messageBytes, written - 4, messageLength, channelListener);
                }
                if(written - 4 > messageLength){
                    throw new IllegalStateException("Written more than expected");
                }
                if(written - 4 == messageLength){
                    // System.out.println(written + "== messageLength");
                    listener.sent(messageBytes);
                    if(queue.size() > 0){
                        // System.out.println("new message");
                        Message nextMessage = queue.remove(0);
                        Sender sender = new Sender(channel, listener, nextMessage.bytes, nextMessage.offset, nextMessage.length);
                        EventPump.getInstance().post(sender);
                    } else {
                        sending = false;
                    }
                    return;
                }
            }
        }
    }

    class Receive implements Runnable{
        final Channel channel;
        final Listener listener;
        byte[] lengthMessage = new byte[4];
        int read = 0;
        int length = 1;
        byte[] message;
        ChannelReadListener channelListener;
        Receive parentReceiver = this;

        Receive(Channel channel, Listener listener){
            this.channel = channel;
            this.listener  = listener;
            this.channelListener = new ChannelReadListener();
        }

        class ChannelReadListener implements Channel.ReadListener{
            @Override
            public void read(int read){
                parentReceiver.read += read;
                // System.out.println("listened"+ parentReceiver.read);
                EventPump.getInstance().post(parentReceiver);
            }
            @Override
            public void disconnected(){
                listener.closed();
            }
        }

        @Override
        public void run() throws IllegalStateException {
            
            if(read < 4){
                channel.read(lengthMessage, read, 4-read, channelListener);
            } else if(read == 4){
                length = byteArrayToInt(lengthMessage);
                this.message = new byte[length];
                channel.read(message, read - 4, length, channelListener);
            } else {
                if(read - 4 < length){
                    // System.out.println(read+" < length");
                    channel.read(message, read - 4, length, channelListener);
                } 
                if(read - 4 > length){
                    throw new IllegalStateException("Read more than expected");
                }
                if(read - 4 == length){
                    listener.received(message);
                    read = 0;
                    length = 0;
                    this.message = new byte[0];
                    EventPump.getInstance().post(this);
                }
            }
        }
    }
    
    public MessageQueue(Channel channel){
        this.channel = channel;
        this.queue = new LinkedList<Message>();
    }

    public interface Listener {
        void received(byte[] msg);
        void sent(byte[] msg);
        void closed();
    }

    public void setListener(Listener l){
        this.listener = l;
        Receive receiver = new Receive(channel, this.listener);
        EventPump.getInstance().post(receiver);
    }

    public boolean send(byte[] message, int offset, int length){
        byte[] messageCopy = new byte[length];
        if (channel.disconnected){
            return false;
        } else if(sending){
            System.arraycopy(message, offset, messageCopy, 0, length);
            Message messageObj = new Message(messageCopy, offset, length);
            queue.add(messageObj);
        } else {
            System.arraycopy(message, offset, messageCopy, 0, length);
            Sender sender = new Sender(channel, listener, messageCopy, 0, length);
            EventPump.getInstance().post(sender);
            sending = true;
        }
        return true;
    }

    void close(){
        channel.disconnect();
    }

    boolean closed(){
        return channel.disconnected;
    };
}
