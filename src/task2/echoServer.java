package task2;
import task1.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        QueueBroker queueBrokerClient = new QueueBroker(brokerClient);
        QueueBroker queueBrokerServer =  new QueueBroker(brokerServer);

        Task echoserver = new Task(brokerServer, queueBrokerServer, new Runnable(){ 
            @Override
            public void run(){
                System.out.println("...echoServers broker: " + Task.getBroker().getName());

                MessageQueue msgQueue = null;

                while(msgQueue == null){
                    try {
                        System.out.println("... echoServer: accepting brokerClient ...");
                        msgQueue = queueBrokerServer.accept(PORT);
                    } catch (InterruptedException e) {  };
                }
                if(msgQueue != null){
                    System.out.println("... echoServer: connected ...");
                }

                //SERVER READ
                byte[] message = msgQueue.receive();
            
                // SERVER WRITE
                if(message != null){
                    msgQueue.send(message, 0, message.length);
                } 
                /* else{
                    System.out.println("Failure.");
                    }
                */
            }
        });

        Task echoclient = new Task(brokerClient, queueBrokerClient, new Runnable(){
            @Override
            public void run(){
                MessageQueue msgQueue = null;
                while (msgQueue == null){
                    try {
                        System.out.println("... echoCllient: connecting to brokerServer ...");
                        msgQueue = queueBrokerClient.connect("brokerServer", PORT);
                    } catch (InterruptedException e) {
                        System.out.println("... echoCllient: Interrupted: Return");
                        
                    }
                }
                if(msgQueue != null){
                    System.out.println("... echoCllient: connected ...");
                }
               

                // final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // byte[] buffer = new byte[32 * 1024];
                // int bytesRead;
                // try {
                //     while ((bytesRead = System.in.read(buffer)) > 0) {
                //         baos.write(buffer, 0, bytesRead);
                //     }
                // } catch (IOException e) {
                //     return;
                // }
                
                String inputString = "Hello World!";
                byte[] message = inputString.getBytes(StandardCharsets.UTF_8);
                // byte[] message = buffer;
                
                // CLIENT WRITE
                msgQueue.send(message, 0, message.length);
                // CLIENT READ
                byte[] messagereceived = msgQueue.receive();
                System.out.println(new String(messagereceived, StandardCharsets.UTF_8));  
            }
        });
        
        echoserver.start();
        System.out.println("...echoserver start succesfull...");
        echoclient.start();
        System.out.println("...echoclient start succesfull...");

        echoclient.join();
        echoserver.join();

    }
}
