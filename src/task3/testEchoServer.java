package task3;
import java.nio.charset.StandardCharsets;


import task1.*;



public class testEchoServer {
    public static final int PORT = 800;
    public static final String MESSAGE = "Hello world";

    public static void main(String[] args) throws InterruptedException{
        Broker brokerClient = new Broker( "brokerClient");
        Broker brokerServer = new Broker( "brokerServer");
        QueueBroker queueBrokerClient = new QueueBroker("queueBrokerClient", brokerClient);
        QueueBroker queueBrokerServer =  new QueueBroker("queueBrokerServer", brokerServer);

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

        class ServerAcceptListener implements QueueBroker.AcceptListener{
            @Override
            public void accepted(MessageQueue queue) {

                System.out.println("... SERVER accepted ...");
                queueBrokerServer.unbind(PORT);
                queue.setListener(new MessageQueueListener(queue));

            }

            class MessageQueueListener implements MessageQueue.Listener{
                MessageQueue msgQueue;

                MessageQueueListener(MessageQueue messageQueue){
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
                    msgQueue.close();
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

        

        class clientConnectListener implements QueueBroker.ConnectListener{
            byte[] message = MESSAGE.getBytes();
            
            class MessageQueueListener implements MessageQueue.Listener{

            MessageQueue messageQueueClient;
            
            MessageQueueListener(MessageQueue messageQueue){
                this.messageQueueClient = messageQueue;
            }

            @Override
            public void received(byte[] msg) {
                System.out.println("CLIENT Received: " + new String(msg, StandardCharsets.UTF_8));  
                messageQueueClient.close();
                EventPump.getInstance().kill();
            }

            @Override
            public void sent(byte[] msg) {
                System.out.println("CLIENT Sent: " + new String(msg, StandardCharsets.UTF_8));  
            }

            @Override
            public void closed() {
                System.out.println("CLIENT messageQueue Disconnected: ");  
            }

        }

            @Override
            public void connected(MessageQueue msgQueue) {
                System.out.println("... CLIENT connected ...");
                queueBrokerClient.unbind(PORT);
                msgQueue.setListener(new MessageQueueListener(msgQueue));
                msgQueue.send(message, 0, message.length);
            }

            @Override
            public void refused() {
                System.out.println("... CLIENT refused retry ... ");
                queueBrokerClient.connect(brokerServer.getName(), PORT, this);
            }

        }

        System.out.println("... SERVER accepting ..." );
        queueBrokerServer.bind(PORT, new ServerAcceptListener());


        System.out.println("... CLIENT connecting ..." );
        queueBrokerClient.connect(brokerServer.getName(), PORT, new clientConnectListener());

        System.out.println("endofmain");
    }
}
