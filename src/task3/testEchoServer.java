package task3;
import java.nio.charset.StandardCharsets;


import task1.*;
import task1.echoServer;
import task2.*;

public class testEchoServer {
    public static final int PORT = 800;
    public static final String MESSAGE = "Hello world";

    public static void main(){
        Broker brokerClient = new Broker( "brokerClient");
        Broker brokerServer = new Broker( "brokerServer");
        QueueBroker queueBrokerClient = new QueueBroker("queueBrokerClient", brokerClient);
        QueueBroker queueBrokerServer =  new QueueBroker("queueBrokerServer", brokerServer);
        
        
        TaskEvent echoServer = new TaskEvent(queueBrokerServer, new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'run'");
            }
        });

        TaskEvent echoClient = new TaskEvent(queueBrokerClient, new Runnable(){ 
            @Override
            public void run(){
                byte[] message = MESSAGE.getBytes();
                TaskEvent currentTask = (TaskEvent)Thread.currentThread();
                QueueBroker queueBroker = currentTask.queueBroker;
                class clientConnectListener implements QueueBroker.ConnectListener{

                    @Override
                    public void connected(MessageQueue msgQueue) {
                        class messageQueueListener implements MessageQueue.Listener{

                            @Override
                            public void received(byte[] msg) {
                                System.out.println(new String(msg, StandardCharsets.UTF_8));  
                                throw new UnsupportedOperationException("Unimplemented method 'received'");
                            }

                            @Override
                            public void sent(byte[] msg) {
                                throw new UnsupportedOperationException("Unimplemented method 'sent'");
                            }

                            @Override
                            public void closed() {
                                throw new UnsupportedOperationException("Unimplemented method 'closed'");
                            }

                        }
                        msgQueue.send(message, 0, message.length, new messageQueueListener());
                    }

                    @Override
                    public void refused() {
                        
                    }

                }
                queueBroker.connect(brokerServer.getName(), PORT, new clientConnectListener());

            }
        });

        echoServer.start();
        echoClient.start();
    }
}
