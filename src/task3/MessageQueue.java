package task3;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import task1.*;

public class MessageQueue {
    Listener listener;
    Channel channel;
    List<Message> queue;
    Receiver receiver;
    private Broker broker;
    boolean sending = false;
    
    class Message {
        byte[] bytes;
        int offset;
        int length;
        Message(byte[] bytes, int offset, int length){
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        }
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
        final byte[] messageBytes;
        final int messageOffset;
        final int messageLength;


        Sender(Channel channel, Listener listener, byte[] bytes, int offset, int length){
            this.channel = channel;
            this.messageBytes = bytes;
            this.messageOffset = offset;
            this.messageLength = length;
            this.listener = listener;
        }

        @Override
        public void run(){
            byte[] messageLengthByte = intToByteArray(messageLength);
            int written = 0;
            synchronized (this){
                while (written < 4){
                    try {
                        written += channel.write(messageLengthByte, written, 4);
                    } catch (IOException e){
                        listener.closed();
                        return;
                    }
                }

                int offset = messageOffset;
                    while(offset < messageLength){
                        try {
                            offset += channel.write(messageBytes, offset, messageLength);
                             
                        } catch (IOException e) {
                            listener.closed();
                            return;
                        };
                    }
            }
            
            listener.sent(messageBytes);
            if (!queue.isEmpty()){
                Message nextMessage = queue.remove(0);
                Sender sender = new Sender(channel, listener, nextMessage.bytes, nextMessage.offset, nextMessage.length);
                task1.Task senderTask = new task1.Task(broker, sender);
                senderTask.start();
            } else {
                sending = false;
            }
        }
    }

    class Receiver implements Runnable{
        final Channel channel;
        final Listener listener;

        Receiver(Channel channel, Listener listener){
            this.channel = channel;
            this.listener  = listener;
        }
        boolean alive = true;
        @Override
        public void run() {
            byte[] lengthMessage;
            int read;
            while(alive){
                lengthMessage = intToByteArray(0);
                read = 0;
                synchronized (this){
                    while (read < 4){
                        try{
                            read += channel.read(lengthMessage, read, 4);
                        } catch (IOException e) { 
                            listener.closed();
                            return;
                        };
                        
                    }
                    int lengthMessageInt = byteArrayToInt(lengthMessage);
                    byte[] message = new byte[lengthMessageInt];
                    int offsetRead = 0;
                    while(offsetRead < lengthMessageInt){
                        try{
                        offsetRead += channel.read(message, offsetRead, lengthMessageInt);
                        } catch (IOException e) { 
                            listener.closed();
                            return;
                        };
                    }
                    if(alive){
                        listener.received(message);
                    }
                }
            }
        }
        
    }
    
    public MessageQueue(Channel channel, Broker broker){
        this.channel = channel;
        this.broker = broker;
        this.queue = new LinkedList<Message>();
    }

    public interface Listener {
        void received(byte[] msg);
        void sent(byte[] msg);
        void closed();
    }

    public void setListener(Listener l){
        this.listener = l;
        receiver = new Receiver(channel, this.listener);
        task1.Task receiverTask = new task1.Task(broker, receiver);
        receiverTask.start();
    }

    public boolean send(byte[] message, int offset, int length){
        byte[] messageCopy = new byte[length];
        int offsetCopy = offset;
        int lengthCopy = length;
        System.arraycopy(message, offset, messageCopy, offsetCopy, lengthCopy);
        if (channel.disconnected){
            return false;
        } else if(sending){
            Message messageObj = new Message(messageCopy, offsetCopy, lengthCopy);
            queue.add(messageObj);
        } else{
            Sender sender = new Sender(channel, listener, messageCopy, offsetCopy, lengthCopy);
            task1.Task senderTask = new task1.Task(broker, sender);
            senderTask.start();
            sending = true; 
        }
        return true;
    }

    void close(){
        receiver.alive = false;
        channel.disconnect();
    }

    boolean closed(){
        return channel.disconnected;
    };
}

