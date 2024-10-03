package sar;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Broker {

  BrokerManager brokerManager;
  String brokerName;
  HashMap <Integer, Rendevous> rdvMap;
  private final ReentrantLock rdvmapLock = new ReentrantLock();

  Broker(String name){
    brokerName = name;
    brokerManager = BrokerManager.getSelf();
    brokerManager.add(this);
    rdvMap = new HashMap<Integer, Rendevous>();
  };
  String getName(){
    return brokerName;
  };
  BrokerManager getBrokerManager(){
    return brokerManager;
  };
  Channel accept(int port) throws InterruptedException {
    synchronized (rdvmapLock) {
      Rendevous rdv = rdvMap.get(port);
      if (rdv == null) {
          rdv = new Rendevous(this, null);
          rdvMap.put(port, rdv);
          rdvmapLock.notifyAll();
      }
      if (rdv.ac != null) {
          throw new IllegalStateException("Busy on port" + port);
      }
      return rdv.accept(this);
  }
  }



  Channel connect(String name, int port) throws InterruptedException{
    Broker rBroker = brokerManager.get(name);
    Rendevous rdv = rBroker.rdvMap.get(port);
    while (rdv == null){
      rdv = rBroker.rdvMap.get(port);
      wait();
    }
    if (rdv.cc != null) {
        throw new IllegalStateException("Broker " + name + " is busy on port" + port);
    }
    return rdv.connect(this);
  }
}