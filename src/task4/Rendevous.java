package task4;

import task1.*;
import task3.EventPump;

public class Rendevous {
    Broker ac;
    Broker cc;
    CircularBuffer CB1;
    CircularBuffer CB2;
    Channel chAccept;
    Channel chConnect;


    Rendevous(Broker a, Broker c){
        ac = null;
        cc = null;
        CB1 = new CircularBuffer(Channel.CAPACITY);
        CB2 = new CircularBuffer(Channel.CAPACITY);
        chAccept = new Channel(CB2, CB1);
        chConnect = new Channel(CB1, CB2);
        chAccept.addRemote(chConnect);
        chConnect.addRemote(chAccept);
    };

    interface AcceptListener {
        void accepted(Channel channel);
    }
    interface ConnectListener {
        void connected(Channel channel);
    }
    void accept(Broker b, AcceptListener listener) {
        ac = b;
        if (cc == null) {
            EventPump.getInstance().post(new Runnable() {
                @Override
                public void run() {
                   accept(b, listener);
                }
            });
        } else {
            //meeting point
            listener.accepted(chAccept);
        }
    }

    void connect(Broker b, ConnectListener listener) {  
        cc = b;
        if (ac == null) {
            EventPump.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    connect(b, listener);                                                 //not sure if this is correct
                }
            });
        } else {
            //meeting point
            listener.connected(chConnect);
        }
    }

    boolean isFull() {
        return (ac != null && cc != null);
    }

}
