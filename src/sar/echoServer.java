package sar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class echoServer {
    public static final int PORT = 800;

    public static byte[] convertIntToByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }

    public static int convertByteArrayToInt(byte[] byteArray) {
        int value = 0;
        value += byteArray[3];
        value += byteArray[2] * Math.pow(2,8);
        value += byteArray[1] * Math.pow(2,16);
        value += byteArray[0] * Math.pow(2,24);
        return value;
        }

   
	public static void main(String[] args) throws IOException, InterruptedException {
        Broker brokerClient = new Broker( "brokerClient");
        Broker brokerServer = new Broker( "brokerServer");

        Task echoserver = new Task(brokerServer, new Runnable(){ 
            @Override
            public void run(){
                Channel channel = brokerServer.accept(PORT);
                byte[] message = null;
                byte[] lengthMessage = null;  
                while (true) {
                    try{

                        channel.read(lengthMessage, 0, 1);
                        
                        if (lengthMessage != null){
                            int lengthToRead = lengthMessage[0];
                            int lengthRead = 0;
                            int offsetRead = 0;
                            while(message == null || lengthRead < lengthToRead){
                                try{
                                    lengthRead = channel.read(message, offsetRead, lengthToRead);
                                    lengthToRead -= lengthRead;
                                    offsetRead += lengthRead;
                                    notifyAll();
                                } catch (IOException e) { 
                                    // nothing
                                };
                                try {
                                    wait();
                                } catch (InterruptedException e1) {
                                    // nothing
                                }
                            }
                            
                            try {
                                channel.write(lengthMessage, 0, 1);
                            } catch (IOException e){
                                //nothing
                            }
                            int lengthWritten = 0;
                            int lengthToWrite = message.length;
                            int offset = 0;
                            while(lengthWritten < lengthToWrite){
                                try {
                                    lengthWritten = channel.write(message, offset, lengthToWrite);
                                    lengthToWrite -= lengthWritten;
                                    offset+= lengthWritten;
                                    notifyAll();
                                } catch (IOException e) {
                                    //nothing
                                };
                                try {
                                    wait();
                                } catch (InterruptedException e1) {
                                    // nothing
                                }
                            }
                        }
                    }
                    catch (IOException e){
                        //nothing
                    };
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // nothing
                    }
                }
            }
        });

        Task echoclient = new Task(brokerClient, new Runnable(){
            @Override
            public void run(){
                Channel channel;
                try {
                    channel = brokerClient.connect("brokerServer", PORT);
                } catch (InterruptedException e) {
                    return;
                }

                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[32 * 1024];
                int bytesRead;
                try {
                    while ((bytesRead = System.in.read(buffer)) > 0) {
                        baos.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    return;
                }

                byte[] message = baos.toByteArray();
                
               
                byte[] lengthMessage = {(byte)message.length, 0, 0, 0};

                try {
                    channel.write(lengthMessage, 0, 1);
                    notifyAll();
                } catch (IOException e){
                    //nothing
                }

                int lengthWritten = 0;
                int lengthToWrite = message.length;
                int offset = 0;
                while(lengthWritten < lengthToWrite){
                    try {
                        lengthWritten = channel.write(message, offset, lengthToWrite);
                        lengthToWrite -= lengthWritten;
                        offset+= lengthWritten;
                        notifyAll();
                    } catch (IOException e) {
                        //nothing
                    };
                    try {
                        wait();
                    } catch (InterruptedException e1) {
                        // nothing
                    }
                }

                lengthMessage = new byte[1];
                
                try{
                    channel.read(lengthMessage, 0, 1);
                    
                    if (lengthMessage != null){
                        int lengthToRead = lengthMessage[0];
                        int lengthRead = 0;
                        int offsetRead = 0;
                        while(message == null || lengthRead < lengthToRead){
                            try{
                                lengthRead = channel.read(message, offsetRead, lengthToRead);
                                lengthToRead -= lengthRead;
                                offsetRead += lengthRead;
                                notifyAll();
                            } catch (IOException e) { 
                                // nothing
                            };
                            try {
                                wait();
                            } catch (InterruptedException e1) {
                                // nothing
                            }
                        }
                    }
                } catch (IOException e) { 
                    // nothing
                };
            }
        });
        
        echoserver.start();
        echoclient.start();

        echoclient.join();
        echoserver.join();

    }
}
