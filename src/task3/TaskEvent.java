package task3;
import task1.*;
import task2.*;

class TaskEvent extends Thread{
    QueueBroker queueBroker;
    EventPump eventPump;
    void post(Runnable r){
        //TODO
    };
    static TaskEvent taskEvent(QueueBroker queueBroker){
        return new TaskEvent();
    };
    void kill(){
        //TODO
    };
    boolean killed(){
        //TODO
        return true;
    };
    }