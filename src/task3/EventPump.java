package task3;

import java.util.LinkedList;
import java.util.List;


public class EventPump extends Thread {
    List<Runnable> pumpList;
    Object lock = new Object();
    static EventPump instance;

    
    private EventPump() {
        this.pumpList = new LinkedList<Runnable>();
    }

    public static EventPump getInstance() {
        if (EventPump.instance == null) {
            System.out.println("EVENTPUMP BEING CREATED");
            EventPump.instance = new EventPump();
            EventPump.instance.start();
        }

        return EventPump.instance;
    }


    public synchronized void run() {
        Runnable nextEvent;
        while(true) {
            synchronized (lock) {
                if(!pumpList.isEmpty()){
                    System.out.println("... EVENTPUMP running event ... ");
                    System.out.println("... pump size befor:" + pumpList.size());
                    nextEvent = pumpList.removeFirst();
                    System.out.println("... pump size after:" + pumpList.size());
                    nextEvent.run();
                    lock.notify();
                } else {
                    System.out.println("... EVENTPUMP waiting ...");
                    try {lock.wait(2000);} catch (InterruptedException e) {}
                }
            }
        }
    }
    public void post(Runnable event) {
        System.out.println("... trying to post... ");
        synchronized (lock) {
            lock.notify();
            System.out.println("... EVENTPUMP adding event ...");
            pumpList.add(event);
        }
    }
}