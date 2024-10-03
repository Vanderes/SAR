package sar;
public class Task extends Thread {

  static Broker broker;
  Runnable runnable;

  Task(Broker b, Runnable r){
    broker = b;
    runnable = r;
  };

  @Override
  public void run(){
    this.runnable.run();
  }

  public static Broker getBroker(){
    return broker;
    
  };   
}