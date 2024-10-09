# Specification for an event-based version
### Overview:

*A module for enabling non-blocking communication between brokers.*


### Queuebroker:

*An abstract class representing a queue broker, responsible for establishing messagequeues.*

    abstract class QueueBroker{


        // Constructor to initialize the QueueBroker with a name
        QueueBroker(String name);

        // Interface for handling accepted connections
        interface AcceptListener {
            void accepted(MessageQueue queue);
        }

        // Method to bind the broker to a specific port and set an accept listener
        boolean bind(int port, AcceptListener listener);

        // Method to unbind the broker from a specific port
        boolean unbind(int port);

        // Interface for handling connection events
        interface ConnectListener {
            void connected(MessageQueue queue);
            void refused();
        }

        // Method to connect to a queue broker by name and port, and set a connect listener
        boolean connect(String name, int port, ConnectListener listener);    
        }


### MessageQueue
*An abstract class representing a messagequeue.*

    abstract class MessageQueue {
    
        // Interface for handling received messages and closed events
        interface Listener {
            void received(byte[] msg);
            void closed();
        }

        // Method to set the listener for the message queue
        void setListener(Listener l);

        // Method to send a message as a byte array
        boolean send(byte[] bytes);

        // Overloaded method to send a portion of a byte array as a message
        boolean send(byte[] bytes, int offset, int length);

        // Method to close the message queue
        void close();

        // Method to check if the message queue is closed
        boolean closed();
    }
