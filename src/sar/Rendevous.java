package sar;

import java.util.List;

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

    synchronized void waitForBothBrokers() throws InterruptedException {
        while (ac == null || cc == null) {
            System.out.println("... waitForBoth:  waiting ...");
            try {wait();} catch (InterruptedException e) {};
        }
        notifyAll();
        System.out.println("... waitForBoth:  both present ...");
    }

    Channel accept(Broker b) throws InterruptedException {
        synchronized (this) {
            System.out.println("... accept rdv:  set AC ...");
            ac = b;
            notifyAll();
        }
        System.out.println("... accept rdv:  waitForBoth ...");
        waitForBothBrokers();
        return chAccept;
    }

    Channel connect(Broker b) throws InterruptedException {
        synchronized (this) {
            System.out.println("... connect rdv:  set CC ...");
            cc = b;
            notifyAll();
        }
        System.out.println("... connect rdv:  waitForBoth ...");
        waitForBothBrokers();
        
        return chConnect;
    }

}
