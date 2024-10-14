package task3;
import java.io.IOException;
import task1.*;

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
    private Broker broker;

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
            byte[] messageLengthByte = {(byte)messageLength};
            int written = 0;
            System.out.println("... sender: running ...");
            synchronized (this){
                while (written ==0){
                    try {
                        System.out.println("... sender: write length to channel ...");
                        written = channel.write(messageLengthByte, 0, 1);
                    } catch (IOException e){
                        //nothing
                    }
                }
            
                int lengthWritten = 0;
                int lengthToWrite = messageLength;
                int offset = messageOffset;
                    while(offset < messageLength){
                        try {
                            System.out.println("... sender: write " + offset + " of " + messageLength + " to channel ...");
                            lengthWritten = channel.write(messageBytes, offset, lengthToWrite);
                            lengthToWrite -= lengthWritten;
                            offset+= lengthWritten;
                        } catch (IOException e) {
                            //nothing
                        };
                    }
            }
            listener.sent(messageBytes);
        }
    }

    class Receiver implements Runnable{
        final Channel channel;
        final Listener listener;

        Receiver(Channel channel, Listener listener){
            this.channel = channel;
            this.listener  = listener;
        }

        @Override
        public void run() {
            byte[] lengthMessage;
            int read;
            while(true){

                System.out.println("trying to read");
                lengthMessage = new byte[1];
                read = 0;
                synchronized (this){
                    while (read == 0){
                        try{
                            read = channel.read(lengthMessage, 0, 1);
                        } catch (IOException e) { 
                            // nothing
                        };
                        
                    }
                    byte[] message = new byte[lengthMessage[0]];
                    int lengthToRead = lengthMessage[0];
                    int lengthRead = 0;
                    int offsetRead = 0;
                    while(offsetRead < lengthMessage[0]){
                        try{
                        lengthRead = channel.read(message, offsetRead, lengthToRead);
                        lengthToRead -= lengthRead;
                        offsetRead += lengthRead;
                        } catch (IOException e) { 
                            // nothing
                        };
                    }
                    listener.received(message);
                }
            }
        }
        
    }
    public MessageQueue(Channel channel, Broker broker){
        this.channel = channel;
        this.broker = broker;
        // this.queue = new LinkedList<Message>();
    }

    interface Listener {
        void received(byte[] msg);
        void sent(byte[] msg);
        void closed();
    }

    void setListener(Listener l){
        this.listener = l;
        Receiver receiver = new Receiver(channel, this.listener);
        task1.Task receiverTask = new task1.Task(broker, receiver);
        receiverTask.start();
        System.out.println("... listener set ...");
    }

    boolean send(byte[] message, int offset, int length){
        System.out.println("... sending ...");
        Sender sender = new Sender(channel, listener, message, offset, length);
        task1.Task senderTask = new task1.Task(broker, sender);
        senderTask.start();
    return true;
    }

    void close(){
        channel.disconnect();
    }

    boolean closed(){
        return channel.disconnected;
    };
}

