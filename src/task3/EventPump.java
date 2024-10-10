package task3;

import java.util.LinkedList;
import java.util.List;


public class EventPump extends Thread {
    List<Runnable> pumpList;
    static EventPump instance;
    
    private EventPump() {
        this.pumpList = new LinkedList<Runnable>();
    }

    public static EventPump getInstance() {
        if (EventPump.instance == null) {
            EventPump.instance = new EventPump();
        }
        return EventPump.instance;
    }


    public synchronized void run() {
        Runnable nextEvent;
        while(true) {
            if(!pumpList.isEmpty()){
                nextEvent = pumpList.removeFirst();
                nextEvent.run();
            } else {
                sleep();
            }
        }
    }
    public synchronized void post(Runnable event) {
        pumpList.add(event);
        notify();
    }
    private void sleep() {
        try {
            wait();
        } catch (InterruptedException ex){
        // nothing to do here.
    }
    }
}