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
    MessageQueue messageQueueAccept;
    MessageQueue messageQueueConnect;
    boolean full;


    Rendevous(Broker a, Broker c){
        ac = null;
        cc = null;
        CB1 = new CircularBuffer(Channel.CAPACITY);
        CB2 = new CircularBuffer(Channel.CAPACITY);
        chAccept = new Channel(CB2, CB1);
        chConnect = new Channel(CB1, CB2);
        chAccept.addRemote(chConnect);
        chConnect.addRemote(chAccept);
        messageQueueAccept = new MessageQueue(chAccept, c);
        messageQueueConnect = new MessageQueue(chConnect, a);
        boolean full = false;
    };


    MessageQueue accept(Broker b) throws InterruptedException {
        synchronized (this) {
            ac = b;
            notifyAll();
        }
        if (cc == null) {
            EventPump.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        accept(b);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        } else {
            full = true;
            return messageQueueAccept;}
    }

    MessageQueue connect(Broker b) throws InterruptedException {
        synchronized (this) {
            cc = b;
            notifyAll();
        }
        if (ac == null) {
            EventPump.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        connect(b);                                                 //not sure if this is correct
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        } else {
            full = true;
            return messageQueueConnect;
        }
    }

}
