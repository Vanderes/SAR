package sar;

import java.util.concurrent.locks.ReentrantLock;

public class Rendevous {
    Broker ac;
    Broker cc;
    private final ReentrantLock lock = new ReentrantLock();

    Rendevous(Broker a, Broker c){
        ac = null;
        cc = null;
    };
    synchronized void setAc(Broker broker) {
        ac = broker;
        lock.notifyAll();
    }

    synchronized void setCc(Broker broker) {
        cc = broker;
        lock.notifyAll();
    }

    synchronized boolean waitForBothBrokers() throws InterruptedException {
        while (ac == null || cc == null) {
            lock.wait();
        }
        return true;
    }

    Channel accept(Broker b) throws InterruptedException {
        setAc(b);
        return waitForBothBrokers() ? new Channel() : null;
    }

    Channel connect(Broker b) throws InterruptedException {
        setCc(b);
        return waitForBothBrokers() ? new Channel() : null;
    }

}
