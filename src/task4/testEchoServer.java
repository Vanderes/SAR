package task4;

import java.nio.charset.StandardCharsets;
import task3.*;

public class testEchoServer {
    public static final int PORT = 800;
    public static final String MESSAGE1 = "Hellllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllo";
    public static final String MESSAGE2 = "World";
    public static final String MESSAGE3 = "Howwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww";
    public static final String MESSAGE4 = "Arrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrre";
    public static final String MESSAGE5 = "You";

    public static void main(String[] args) throws InterruptedException{
        Broker brokerClient = new Broker( "brokerClient");
        Broker brokerServer = new Broker( "brokerServer");
        QueueBroker queueBrokerClient = new QueueBroker("queueBrokerClient", brokerClient);
        QueueBroker queueBrokerServer =  new QueueBroker("queueBrokerServer", brokerServer);

        EventPump eventPump = EventPump.getInstance();

        class ServerAcceptListener implements QueueBroker.AcceptListener{
            @Override
            public void accepted(MessageQueue queue) {
                eventPump.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("... SERVER accepted ...");
                        queue.setListener(new MessageQueueListener(queue));
                    }
                });
            }

            class MessageQueueListener implements MessageQueue.Listener{
                MessageQueue msgQueue;

                MessageQueueListener(MessageQueue messageQueue){
                    this.msgQueue = messageQueue;
                }

                @Override
                public void received(byte[] msg) {
                    eventPump.post(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("SERVER Received: " + new String(msg, StandardCharsets.UTF_8));  
                            msgQueue.send(msg, 0, msg.length);
                        }
                    });
                }

                @Override
                public void sent(byte[] msg) {
                    eventPump.post(new Runnable() {
                        @Override
                        public void run() {
                            //System.out.println("SERVER Sent: " + new String(msg, StandardCharsets.UTF_8));  
                        }
                    });
                }

                @Override
                public void closed() {
                    eventPump.post(new Runnable() {
                        @Override
                        public void run() {
                        System.out.println("SERVER messageQueue Disconnected: ");  
                        }
                    });
                }
            }
        }

        class clientConnectListener implements QueueBroker.ConnectListener{
            byte[] message1 = MESSAGE1.getBytes();
            byte[] message2 = MESSAGE2.getBytes();
            byte[] message3 = MESSAGE3.getBytes();
            byte[] message4 = MESSAGE4.getBytes();
            byte[] message5 = MESSAGE5.getBytes();
            
            @Override
            public void connected(MessageQueue msgQueue) {
                eventPump.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("... CLIENT connected ...");
                        msgQueue.setListener(new messageQueueListener());
                        msgQueue.send(message1, 0, message1.length);
                        msgQueue.send(message2, 0, message2.length);
                        msgQueue.send(message3, 0, message3.length);
                        msgQueue.send(message4, 0, message4.length);
                        msgQueue.send(message5, 0, message5.length);
                    }
                });
            }

            @Override
            public void refused() {
                final clientConnectListener parentListener = this;
                eventPump.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("... CLIENT refused retry ... ");
                        queueBrokerClient.connect(brokerServer.getName(), PORT, parentListener);
                    }
                });
            }
            
            class messageQueueListener implements MessageQueue.Listener{

                @Override
                public void received(byte[] msg) {
                    eventPump.post(new Runnable() {
                        @Override
                        public void run() {
                            String receivedMsg = new String(msg, StandardCharsets.UTF_8);
                            System.out.println("CLIENT Received: " + receivedMsg);  
                            // if (receivedMsg.equals(MESSAGE)){
                            //     System.out.println("SUCCES");
                            //     eventPump.kill();
                            // }
                        }
                    });
                }
    
                @Override
                public void sent(byte[] msg) {
                    eventPump.post(new Runnable() {
                        @Override
                        public void run() {
                            //System.out.println("CLIENT Sent: " + new String(msg, StandardCharsets.UTF_8));  
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
