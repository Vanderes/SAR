package task3;

import java.util.HashMap;

import task1.*;

public class QueueBroker {
    Broker broker;
    String name;
    HashMap <Integer, Acceptor> acceptorMap;
    HashMap <Integer, Connector> connectorMap;

    public QueueBroker(String name, Broker broker){
        this.name = name;
        this.broker = broker;
        this.acceptorMap = new HashMap<>();
        this.connectorMap = new HashMap<>();
    };

    class Acceptor implements Runnable{
        boolean run;
        int port;
        AcceptListener listener;

        Acceptor(int port, AcceptListener listener){
            this.port = port;
            this.listener=listener;
            run=true;
        }
        void stop(){run=false;}

        @Override
        public void run(){
            Channel channel;
            while(run){
                try {
                    channel = broker.accept(port);
                    final Channel finalChannel = channel;
                    EventPump.getInstance().post(new Runnable() {

                        @Override
                        public void run() {
                            MessageQueue mq = new MessageQueue(finalChannel, broker);
                            listener.accepted(mq);
                        }
                    });
                } catch (IllegalStateException e) {
                    //nothing
                } catch (InterruptedException e) {
                    //nothing
                }
                

                // Event event = new Event(((TaskEvent)Thread.currentThread()), new Runnable() {
                //     MessageQueue mq = new MessageQueue();

                //     @Override
                //     public void run() {
                //         listener.accepted(mq);
                //     }}
                // );
                
                
            }

        }
    }

    class Connector implements Runnable{
        boolean run;
        int port;
        ConnectListener listener;
        String name;

        Connector(String name, int port, ConnectListener listener){
            this.port = port;
            this.listener=listener;
            this.name = name;
            run=true;
        }
        void stop(){run=false;}

        @Override
        public void run(){
            Channel channel;
            try {
                channel = broker.connect(name, port);
                final Channel finalChannel = channel;
                EventPump.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        MessageQueue mq = new MessageQueue(finalChannel, broker);
                        listener.connected(mq);
                    }}
                );
            } catch (IllegalStateException e) {
                listener.refused();
            } catch (InterruptedException e) {
                listener.refused();
            }
        }

    }


    public interface AcceptListener {
        void accepted(MessageQueue queue);
    }

    public interface ConnectListener {
        void connected(MessageQueue queue);
        void refused();
    }
    
    public boolean bind(int port, AcceptListener listener){
        Acceptor acceptor = new Acceptor(port, listener);
        task1.Task acceptorTask = new task1.Task(broker, acceptor);
        acceptorMap.put(port, acceptor);
        acceptorTask.start();
        return true;
    };

    boolean unbind(int port){
        Acceptor acceptor = acceptorMap.get(port);
        if (acceptor == null) {
            return false;
        }
        acceptor.stop();
        return true;
    };
    
    public boolean connect(String name, int port, ConnectListener listener){
        Connector connector = new Connector(name, port, listener);
        task1.Task connectorTask = new task1.Task(broker, connector);
        connectorMap.put(port, connector);
        connectorTask.start();
        return true;
    };
}

// class qbAcceptListener implements Acceptlistener{
//     public void accepted(MessageQueue queue){

//     };
// }

// class qbConnectListener implements ConnectListener{

//     public void connected(MessageQueue queue){

//     };
//     public void refused(){

//     };
// }