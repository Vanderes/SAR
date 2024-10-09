package task3;
import task1.*;
import task2.*;

class TaskEvent extends Thread{
    
    
    void post(Runnable r){
        //TODO
    };
    static TaskEvent taskEvent(){
        //TODO;
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