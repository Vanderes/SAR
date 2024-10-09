package task3;
import java.util.HashMap;

import task1.*;
import task2.*;

public class QueueBroker {
    Broker broker;
    String name;
    HashMap <Integer, Acceptor> acceptors;

    QueueBroker(String name, Broker broker){
        this.name = name;
        this.broker = broker;
        this.acceptors = new HashMap<>();
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
            TaskEvent currentTaskEvent = (TaskEvent)Thread.currentThread();
            while(run){
                try {
                    channel = broker.accept(port);
                    final Channel finalChannel = channel;
                    currentTaskEvent.post(new Runnable() {

                        @Override
                        public void run() {
                            MessageQueue mq = new MessageQueue(finalChannel);
                            listener.accepted(mq);
                        }}
                    );
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



    interface AcceptListener {
        void accepted(MessageQueue queue);
    }
    
    boolean bind(int port, AcceptListener listener){
        Acceptor acceptor = new Acceptor(port, listener);
        task1.Task acceptorTask = new task1.Task(broker, acceptor);
        acceptors.put(port, acceptor);
        acceptorTask.start();
        return true;
    };
    boolean unbind(int port){
        //todo
        Acceptor acceptor = acceptors.get(port);
        if (acceptor == null) {
            return false;
        }
        acceptor.stop();
        return true;
    };
    
    interface ConnectListener {
        void connected(MessageQueue queue);
        void refused();
    }
    
    boolean connect(String name, int port, ConnectListener listener){
        //todo
        return true;
    };
}
