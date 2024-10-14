package task3;


class TaskEvent extends Thread{
    QueueBroker queueBroker;
    Runnable runnable;

    public TaskEvent(QueueBroker queueBroker, Runnable runnable) {
        this.queueBroker = queueBroker;
        this.runnable = runnable;
    }
    void post(Runnable r){
        //TODO
    };

    // static TaskEvent taskEvent(QueueBroker queueBroker){
    //     return new TaskEvent();
    // };

    void kill(){
        //TODO
    };
    boolean killed(){
        //TODO
        return true;
    };
    public static QueueBroker getQueueBroker(){
    var currentThread = Thread.currentThread();
    return ((TaskEvent) currentThread).queueBroker;
  }
    }