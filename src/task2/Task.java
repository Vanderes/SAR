package task2;
import task1.*;
public class Task extends Thread {

  Broker broker;
  QueueBroker queueBroker;
  Runnable runnable;

  Task(Broker b,QueueBroker qb, Runnable r){
    broker = b;
    queueBroker = qb;
    runnable = r;
  };

  @Override
  public void run(){
    this.runnable.run();
  }

  public static Broker getBroker(){
    var currentThread = Thread.currentThread();
    return ((Task) currentThread).broker;
  }
  public static QueueBroker getQueueBroker(){
    var currentThread = Thread.currentThread();
    return ((Task) currentThread).queueBroker;
  }

}