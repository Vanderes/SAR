package task3;

import java.util.LinkedList;
import java.util.List;

// public class EventPump extends Thread, which is a thread that runs a list of Runnable objects, one at a time. Only one EventPump object can exist at a time.
public class EventPump extends Thread {
    List<Runnable> pumpList;
    Object lock = new Object();
    boolean dead = false;
    private static volatile EventPump instance;

    private EventPump() {
        this.pumpList = new LinkedList<>();
    }

    public static EventPump getInstance() {
        if (instance == null) {
            synchronized (EventPump.class) {
                if (instance == null) {
                    instance = new EventPump();
                    instance.start();
                }
            }
        }
        return instance;
    }

    // public void run(), which runs the list of Runnable objects, one at a time. The method waits for a new Runnable object to be added to the list if the list is empty.
    public synchronized void run() {
        Runnable nextEvent;
        while(!dead || !pumpList.isEmpty()) {
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
        if(!dead){
            synchronized (lock) {
                lock.notify();
                pumpList.add(event);
            }
        }
    }

    public void kill() {
        System.out.println("killing");;
        synchronized (lock) {
            dead = true;
            lock.notify();
        }
    }
}