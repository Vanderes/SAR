package task3;

public class Event {
    TaskEvent creatorTask;
    Runnable runnable;
    public Event(TaskEvent creatorTask, Runnable runnable){
        this.creatorTask = creatorTask;
        this.runnable = runnable;
    }

}
