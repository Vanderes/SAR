package sar;

import java.util.HashMap;

abstract class Broker {

  BrokerManager brokerManager;
  String brokerName;
  HashMap <Integer, Rendevous> rdvMap;

  Broker(String name, BrokerManager bm){
    brokerName = name;
    brokerManager = bm;
    rdvMap = new HashMap<Integer, Rendevous>();
  };
  String getName(){
    return brokerName;
  };
  BrokerManager getBrokerManager(){
    return brokerManager;
  };

  Channel accept(int port){
    Rendevous rdv = rdvMap.get(port);
    if (rdv == null){
      rdv = new Rendevous(this, null);
      rdvMap.put(port, rdv);
    }
    if (rdv.ac != null) {
      throw new IllegalStateException("Busy on port" + port);
    }
    return rdv.accept(this);
  };

  Channel connect(String name, int port){
    Broker rBroker = brokerManager.get(name);
    Rendevous rdv = rBroker.rdvMap.get(port);
    if (rdv.cc != null) {
        throw new IllegalStateException("Broker " + name + " is busy on port" + port);
    }
    return rdv.connect(this);
  }
}