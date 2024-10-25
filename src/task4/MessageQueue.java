package task4;
import task3.*;
import java.io.IOException;

public class MessageQueue {
    Listener listener;
    Channel channel;
    // List<Message> queue;
    // class Message {
    //     byte[] bytes;
    //     int offset;
    //     int length;
    //     public void Messsage(byte[] bytes, int offset, int length){
    //         this.bytes = bytes;
    //         this.offset = offset;
    //         this.length = length;
    //     }
    // }

    class Sender implements Runnable{
        final Channel channel;
        final Listener listener;
        byte[] messageBytes;
        int messageLength;
        int written = 0;


        Sender(Channel channel, Listener listener, byte[] bytes, int offset, int length){
            this.channel = channel;
            this.messageBytes = bytes;
            this.messageLength = length;
            this.listener = listener;
        }

        @Override
        public void run(){
            if(written == 0){
                byte[] messageLengthByte = {(byte)messageLength};
                try {
                    if((written = channel.write(messageLengthByte, 0, 1))==1){
                    };
                } catch (IOException e){
                    return;
                }
            }
            if(written > 0){
                if(written - 1 < messageLength){
                    try {
                        written += channel.write(messageBytes, written - 1, messageLength);
                    } catch (IOException e) {
                        return;
                    };
                }
                if(written - 1 > messageLength){
                    throw new IllegalStateException("Written more than expected");
                }
                if(written - 1 == messageLength){
                    listener.sent(messageBytes);
                    written = 0;
                    return;
                }
            }
            EventPump.getInstance().post(this);
        }
    }

    class Receive implements Runnable{
        final Channel channel;
        final Listener listener;
        byte[] lengthMessage = new byte[1];
        int read = 0;
        int length = 1;
        byte[] message;

        Receive(Channel channel, Listener listener){
            this.channel = channel;
            this.listener  = listener;

        }

        @Override
        public void run() throws IllegalStateException {
            
            if(read == 0){
               try{
                    if ((read += channel.read(lengthMessage, 0, 1)) == 1){
                        length = lengthMessage[0];
                        this.message = new byte[length];
                    };
                    // WHAT if read > 1? not my responsability?
                } catch (IOException e) { 
                    // nothing
                    return;
                };
            }
            if(read > 0){
                if(read - 1 < length){
                    try{
                    read += channel.read(message, read-1, length);
                    } catch (IOException e) { 
                        return;
                    };
                } 
                if(read -1 > length){
                    throw new IllegalStateException("Read more than expected");
                }
                if(read - 1 == length){
                    listener.received(message);
                    read = 0;
                    length = 0;
                    this.message = new byte[0];
                }
            }
            EventPump.getInstance().post(this);
        }
    }
    
    public MessageQueue(Channel channel){
        this.channel = channel;
        // this.queue = new LinkedList<Message>();
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
        if (channel.disconnected){
            return false;
        }
        byte[] messageCopy = new byte[length];
        System.arraycopy(message, offset, messageCopy, 0, length);
        Sender sender = new Sender(channel, listener, messageCopy, 0, length);
        EventPump.getInstance().post(sender);
        return true;
    }

    void close(){
        channel.disconnect();
    }

    boolean closed(){
        return channel.disconnected;
    };
}

