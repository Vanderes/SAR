package task2;
import task1.*;

public class QueueBroker {
    Broker broker;
    QueueBroker(Broker broker){
        this.broker = broker;
    };
    String name(){
        return broker.getName();
    };

    MessageQueue accept(int port) throws InterruptedException, IllegalStateException {
        Channel channel;
        channel = broker.accept(port);
        return new MessageQueue(channel);
    };

    MessageQueue connect(String name, int port) throws IllegalStateException, InterruptedException {
        Channel channel = broker.connect(name, port);
        return new MessageQueue(channel);
    };
}
