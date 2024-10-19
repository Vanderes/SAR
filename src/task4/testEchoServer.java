package task4;

import java.nio.charset.StandardCharsets;

import task1.*;
import task2.*;
import task3.*;

public class testEchoServer {
    public static final int PORT = 800;
    public static final String MESSAGE = "Hello world";

    public static void main(String[] args) throws InterruptedException{
        Broker brokerClient = new Broker( "brokerClient");
        Broker brokerServer = new Broker( "brokerServer");
        task3.QueueBroker queueBrokerClient = new task3.QueueBroker("queueBrokerClient", brokerClient);
        task3.QueueBroker queueBrokerServer =  new task3.QueueBroker("queueBrokerServer", brokerServer);

        EventPump eventPump = EventPump.getInstance();
        
        

        // S E R V E R

        /*
        TaskEvent echoServer = new TaskEvent(queueBrokerServer, new Runnable() {

            @Override
            public void run() {
                System.out.println("... SERVER running ...");
                // TaskEvent currentTask = (TaskEvent)Thread.currentThread();
                QueueBroker queueBroker = TaskEvent.getQueueBroker();

                
            }

        }); 
        */

        class ServerAcceptListener implements task3.QueueBroker.AcceptListener{
            @Override
            public void accepted(task3.MessageQueue queue) {

                System.out.println("... SERVER accepted ...");
                queue.setListener(new MessageQueueListener(queue));
            }

            class MessageQueueListener implements task3.MessageQueue.Listener{
                task3.MessageQueue msgQueue;

                MessageQueueListener(task3.MessageQueue messageQueue){
                    this.msgQueue = messageQueue;
                }

                @Override
                public void received(byte[] msg) {
                    System.out.println("SERVER Received: " + new String(msg, StandardCharsets.UTF_8));  
                    msgQueue.send(msg, 0, msg.length);
                }

                @Override
                public void sent(byte[] msg) {
                    System.out.println("SERVER Sent: " + new String(msg, StandardCharsets.UTF_8));  
                }

                @Override
                public void closed() {
                    System.out.println("SERVER messageQueue Disconnected: ");  
                }
            }
        }

        // C L I E N T

        /*
        TaskEvent echoClient = new TaskEvent(queueBrokerClient, new Runnable(){ 
            @Override
            public void run(){
                System.out.println("... CLIENT running");
                byte[] message = MESSAGE.getBytes();
                TaskEvent currentTask = (TaskEvent)Thread.currentThread();
                QueueBroker queueBroker = currentTask.queueBroker;

            }
        });
        */


        class clientConnectListener implements task3.QueueBroker.ConnectListener{
            byte[] message = MESSAGE.getBytes();
            
            class messageQueueListener implements task3.MessageQueue.Listener{

                @Override
                public void received(byte[] msg) {
                    eventPump.post(new Runnable() {
                        @Override
                        public void run() {
                            String receivedMsg = new String(msg, StandardCharsets.UTF_8);
                            System.out.println("CLIENT Received: " + receivedMsg);  
                            if (receivedMsg.equals(MESSAGE)){
                                System.out.println("SUCCES");
                                eventPump.kill();
                            }
                        }
                    });
                }
    
                @Override
                public void sent(byte[] msg) {
                    eventPump.post(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("CLIENT Sent: " + new String(msg, StandardCharsets.UTF_8));  
                        }
                    });
                }
    
                @Override
                public void closed() {
                    eventPump.post(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("CLIENT messageQueue Disconnected: ");  
                        }
                    });
                }
    
            }

            @Override
            public void connected(task3.MessageQueue msgQueue) {
                eventPump.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("... CLIENT connected ...");
                        msgQueue.setListener(new messageQueueListener());
                        msgQueue.send(message, 0, message.length);
                    }
                });
            }

            @Override
            public void refused() {
                final clientConnectListener parenListener = this;
                eventPump.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("... CLIENT refused retry ... ");
                        queueBrokerClient.connect(brokerServer.getName(), PORT, parenListener);
                    }
                });
            }

        }

        eventPump.post(new Runnable() {
            @Override
            public void run() {
                System.out.println("... SERVER accepting ..." );
                queueBrokerServer.bind(PORT, new ServerAcceptListener());
         }
        });

        
        eventPump.post(new Runnable() {
            @Override
            public void run() {
                System.out.println("... CLIENT connecting ..." );
                queueBrokerClient.connect(brokerServer.getName(), PORT, new clientConnectListener());
            }
        });

        System.out.println("endofmain");
    }
}
