package task4;

import java.util.HashMap;
import task3.*;
public class Broker {

  BrokerManager brokerManager;
  String brokerName;
  HashMap <Integer, Rendevous> rdvMap;
  Broker thisBroker = this;

  public Broker(String name){
    brokerName = name;
    brokerManager = BrokerManager.getSelf();
    brokerManager.add(this);
    rdvMap = new HashMap<Integer, Rendevous>();
  };

  public String getName(){
    return brokerName;
  };
  
  public BrokerManager getBrokerManager(){
    return brokerManager;
  };

  public interface  AcceptListener {
    void accepted(Channel channel);
  }

  public interface ConnectListener{
    void connected(Channel channel);
  }

  // if the port is already in use, throw an exception, allready accepting on port is considered "in use"
  // when rdv meets, listener is notified "accepted" and the rendevous is removed from the map
  public void accept(int port, AcceptListener listener) throws IllegalStateException {
    class rdvAcceptListener implements Rendevous.AcceptListener{
      AcceptListener listener;
      rdvAcceptListener(AcceptListener listener){
        this.listener = listener;
      }
      @Override
      public void accepted(Channel channel){
        
        //should be reset?
        rdvMap.remove(port);
        listener.accepted(channel);
      }
    }
    if(rdvMap.containsKey(port)){
      throw new IllegalStateException("Busy on port" + port);    
    } else{
      //should this be posted?
      EventPump.getInstance().post(new Runnable() {
        @Override
        public void run() {
          Rendevous rdv = new Rendevous(thisBroker, null);
          rdvMap.put(port, rdv);
          rdv.accept(thisBroker, new rdvAcceptListener(listener));
        }
      });
    }    
  }



  public void connect(String name, int port, ConnectListener listener) throws IllegalStateException{
    Broker rBroker = brokerManager.get(name);

    class rdvConnectListener implements Rendevous.ConnectListener{
      ConnectListener listener;
      rdvConnectListener(ConnectListener listener){
        this.listener = listener;
      }
      @Override
      public void connected(Channel channel){
        rdvMap.remove(port);
        listener.connected(channel);
      }
    }

    if (rBroker.rdvMap.containsKey(port)){
      Rendevous rdv = rBroker.rdvMap.get(port);
      if (rdv.isFull()) {
        throw new IllegalStateException("Broker " + name + " is busy on port" + port);
      } else{
      rdv.connect(thisBroker, new rdvConnectListener(listener));
      }
      
    } else {
      //should this be posted?
      EventPump.getInstance().post(new Runnable() {
        @Override
        public void run() {
          connect(name, port, listener);
        }
      });
    }
  }
}