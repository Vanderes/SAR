package task3;
import task1.*;
import task2.*;

public class QueueBroker {
    
    QueueBroker(String name){
        //todo
    };

    interface AcceptListener {
        void accepted(MessageQueue queue);
        //todo
    }
    
    boolean bind(int port, AcceptListener listener){
        //todo
        return true;
    };
    boolean unbind(int port){
        //todo
        return true;
    };
    
    interface ConnectListener {
        void connected(MessageQueue queue);
        void refused();
    }
    
    boolean connect(String name, int port, ConnectListener listener){
        //todo
        return true;
    };
}
