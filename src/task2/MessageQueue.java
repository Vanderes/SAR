package task2;
import java.io.IOException;

import task1.*;

public class MessageQueue {
    Channel channel;

    public MessageQueue(Channel channel){
        this.channel = channel;
    }

    void send(byte[] bytes, int offset, int length) {
        byte[] lengthMessage = {(byte)length};
        int written = 0;
        synchronized (this){
            while (written ==0){
                try {
                    written = channel.write(lengthMessage, 0, 1);
                } catch (IOException e){
                    //nothing
                }
            }
        
        int lengthWritten = 0;
        int lengthToWrite = length;
            while(offset < length){
                try {
                    lengthWritten = channel.write(bytes, offset, lengthToWrite);
                    lengthToWrite -= lengthWritten;
                    offset+= lengthWritten;
                } catch (IOException e) {
                    //nothing
                };
            }
        }
    };

    byte[] receive() {
        byte[] lengthMessage = new byte[1];
        int read = 0;
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
            return message;
        }
    };
    void close(){
        channel.disconnect();
    };
    boolean closed(){
        return channel.disconnected;
    };
}
