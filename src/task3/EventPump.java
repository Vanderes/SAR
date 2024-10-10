package task3;

import java.util.LinkedList;
import java.util.List;

public class EventPump extends Thread {
    List<Event> pumpList;

    EventPump() {
        this.pumpList = new LinkedList<Event>();
    }

    public synchronized void run() {
        Event nextEvent;
        while(true) {
            if(!pumpList.isEmpty()){
                nextEvent = pumpList.removeFirst();
                nextEvent.runnable.run();
            } else {
                sleep();
            }
        }
    }
    public synchronized void post(Event event) {
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