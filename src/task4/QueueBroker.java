package task4;

import java.util.HashMap;


import task3.*;

public class QueueBroker {
    Broker broker;
    String name;
    HashMap <Integer, BrokerAcceptListener> acceptorMap;
    HashMap <Integer, BrokerConnectListener> connectorMap;


    public QueueBroker(String name, Broker broker){
        this.name = name;
        this.broker = broker;
        this.acceptorMap = new HashMap<>();
        this.connectorMap = new HashMap<>();
    };

    class BrokerAcceptListener implements Broker.AcceptListener{
        AcceptListener listener;
        int port;
        boolean dead;

        BrokerAcceptListener(AcceptListener listener, int port){
            this.listener = listener;
            this.port = port;
        }

        @Override
        public void accepted(Channel channel ){
            if (!dead){
                MessageQueue mq = new MessageQueue(channel);
                listener.accepted(mq);
                broker.accept(port, this);
            }
        }
        public void kill(){
            dead = true;
        }
    }
    
    class BrokerConnectListener implements Broker.ConnectListener{
        ConnectListener listener;
        int port;

        BrokerConnectListener(ConnectListener listener, int port){
            this.listener = listener;
            this.port = port;
        }

        @Override
        public void connected(Channel channel){
            MessageQueue mq = new MessageQueue(channel);
            listener.connected(mq);
            connectorMap.remove(port);
        }
    }

    public interface AcceptListener {
        void accepted(MessageQueue queue);
    }

    public interface ConnectListener {
        void connected(MessageQueue queue);
        void refused();
    }

    // bind should be indefinite, which is not the case here
    public boolean bind(int port, AcceptListener listener){
        BrokerAcceptListener brokerAcceptListener = new BrokerAcceptListener(listener, port);
        acceptorMap.put(port, brokerAcceptListener);

        //shloud this be posted?
        EventPump.getInstance().post(new Runnable(){
            @Override
            public void run(){
                broker.accept(port, brokerAcceptListener);
            }
        });
        return true;
    };

    boolean unbind(int port){
        if (acceptorMap.containsKey(port)) {
            return false;
        }
        acceptorMap.get(port).kill();
        acceptorMap.remove(port);
        return true;
    };
    
    public boolean connect(String name, int port, ConnectListener listener){
        BrokerConnectListener brokerConnectListener = new BrokerConnectListener(listener, port);    
        connectorMap.put(port, brokerConnectListener);
        EventPump.getInstance().post(new Runnable() {
            @Override
            public void run() {
                broker.connect(name, port, brokerConnectListener);
            }
        });
        return true;
    };
}
