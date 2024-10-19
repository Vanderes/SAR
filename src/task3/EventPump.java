package task3;

import java.util.LinkedList;
import java.util.List;


public class EventPump extends Thread {
    List<Runnable> pumpList;
    Object lock = new Object();
    static EventPump instance;
    boolean dead = false;
    private EventPump() {
        this.pumpList = new LinkedList<Runnable>();
    }

    public static EventPump getInstance() {
        if (EventPump.instance == null) {
            EventPump.instance = new EventPump();
            EventPump.instance.start();
        }

        return EventPump.instance;
    }


    public synchronized void run() {
        Runnable nextEvent;
        while(!dead) {
            synchronized (lock) {
                if(!pumpList.isEmpty()){
                    nextEvent = pumpList.removeFirst();
                    nextEvent.run();
                    lock.notify();
                } else {
                    try {lock.wait(2000);} catch (InterruptedException e) {}
                }
            }
        }
    }
    public void post(Runnable event) {
        synchronized (lock) {
            lock.notify();
            pumpList.add(event);
        }
    }

    public void kill() {
        synchronized (lock) {
            dead = true;
            lock.notify();
        }
    }
}