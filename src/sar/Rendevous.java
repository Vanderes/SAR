package sar;

public class Rendevous {
    Broker ac;
    Broker cc;

    Rendevous(Broker a, Broker c){
        ac = null;
        cc = null;
    };
    Channel accept(Broker b){
        ac = b;
        while(ac == null || cc == null){
            try {
                wait();
            } catch (InterruptedException e) {
                //nothing
            }
        };
        return new Channel();
    };

    Channel connect(Broker b){
        cc = b;
        while(ac == null || cc == null){
            try {
                wait();
            } catch (InterruptedException e) {
                //nothing
            }
        };
        return new Channel();
    };

}
