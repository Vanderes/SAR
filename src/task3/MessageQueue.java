package task3;
import java.io.IOException;

import task1.*;
import task2.*;

public class MessageQueue {
    Listener listener;
    Channel channel;

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
            synchronized (this){
                while (written ==0){
                    try {
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
        
    public MessageQueue(Channel channel){
        this.channel = channel;
    }

    interface Listener {
        void received(byte[] msg);
        void sent(byte[] msg);
        void closed();
    }

    void setListener(Listener l){
        this.listener = l;
    }

    boolean send(byte[] message, int offset, int length, Listener l){
        Sender sender = new Sender(channel, l, message, offset, length);
        sender.run();
    return true;
    }

    void close(){
        channel.disconnect();
    }

    boolean closed(){
        return channel.disconnected;
    };
}

class Message {
    byte[] bytes;
    int offset;
    int length;
}