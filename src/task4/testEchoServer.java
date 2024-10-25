package task4;

import java.nio.charset.StandardCharsets;
import task3.*;

public class testEchoServer {
    public static final int PORT = 800;
    public static final String MESSAGE = "Hello world";

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
                            System.out.println("SERVER Sent: " + new String(msg, StandardCharsets.UTF_8));  
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
            byte[] message = MESSAGE.getBytes();
            
            @Override
            public void connected(MessageQueue msgQueue) {
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
