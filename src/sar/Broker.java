package sar;

import java.util.HashMap;

public class Broker {

  BrokerManager brokerManager;
  String brokerName;
  HashMap <Integer, Rendevous> rdvMap;

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

  Channel accept(int port) throws InterruptedException, IllegalStateException {
    Rendevous rdv = rdvMap.get(port);
    synchronized (this) {
      if (rdv == null) {
        rdv = new Rendevous(this, null);
        rdvMap.put(port, rdv);
      }
      else if (rdv.ac != null) {
          throw new IllegalStateException("Busy on port" + port);
      }
      notifyAll();
    }
    return rdv.accept(this);
  }



  Channel connect(String name, int port) throws InterruptedException, IllegalStateException{
    Broker rBroker = brokerManager.get(name);
    Rendevous rdv = rBroker.rdvMap.get(port);
    synchronized (this) {
      while (rdv == null){
        try{wait(1000);} 
        catch(InterruptedException e){
        };
        rdv = rBroker.rdvMap.get(port);
      }
      if (rdv.cc != null) {
          throw new IllegalStateException("Broker " + name + " is busy on port" + port);
      }
    }
    return rdv.connect(this);
  }
}