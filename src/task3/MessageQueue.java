package task3;
import task1.*;
import task2.*;

public class MessageQueue {
    Listener listener;
    Channel channel;

    public MessageQueue(Channel channel){
        this.channel = channel;
    }

    interface Listener {
        void received(byte[] msg);
        void sent(byte[] msg);
        void closed();
    }
    void setListener(Listener l){
        this.listener = l;
    };
    boolean send(byte[] message, int offset, int length, Listener l){
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