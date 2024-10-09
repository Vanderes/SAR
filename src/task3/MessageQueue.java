package task3;
import task1.*;
import task2.*;

public class MessageQueue {
    Listener listener;
    Channel channel;

    interface Listener {
        void received(byte[] msg);
        void sent(Message msg);
        void closed();
    }
    void setListener(Listener l){
        this.listener = l;
    };
    boolean send(Message msg){
        //TODO
        return true;
    };
    void close(){
        //TODO
    };
    boolean closed(){
        //TODO
        return true;
    };
}

class Message {
    byte[] bytes;
    int offset;
    int length;
}