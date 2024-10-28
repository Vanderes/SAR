package task4;


import task1.*;
import task3.EventPump;

public class Channel {
  
  CircularBuffer in;
  CircularBuffer out;
  Channel rch;
  public Boolean disconnected;

  public static final int CAPACITY = 5;

  Channel (CircularBuffer circularBufferIN, CircularBuffer circularBufferOUT){
    in = circularBufferIN;
    out = circularBufferOUT;
  };

  public void addRemote(Channel remote){
    rch = remote;
    disconnected = false;
  }

  /**
   * Listener
   */
  public interface WriteListener {
    void written(int written);
    void disconnected();
  }
  public interface ReadListener {
    void read(int read);
    void disconnected();
  }

  public void read(byte[] bytes, int offset, int length, ReadListener listener){
    EventPump.getInstance().post(new Runnable() {
      @Override
      public void run() {
        if (disconnected || rch.disconnected){
          listener.disconnected();
        }

        if (out.empty()){
          listener.read(0);
          return;
        }

        int i = 0;
        while(offset + i < length) { 
          try{
            bytes[offset + i] = out.pull();
            i += 1;
          } catch (IllegalStateException e) {
              // System.out.println("read listener e " + i);
              listener.read(i);
              return;
          }
        }
        // System.out.println("read listener " + i);
        listener.read(i);
        return;
      }
    });
  };






  public void write(byte[] bytes, int offset, int length, WriteListener listener) {
    EventPump.getInstance().post(new Runnable(){
      @Override
      public void run(){
        if (disconnected || rch.disconnected){
          listener.disconnected();
          return;
        }
    
        if (in.full()){
          listener.written(0);
          return;
        }
    
        int i = 0;
        while(offset + i < length) { 
          try{
            in.push(bytes[offset + i]);
            i += 1;
          } catch (IllegalStateException e) {
              // System.out.println("listener e " + i);
              listener.written(i);
              return;
          }
        }
        // System.out.println("listener " + i);
        listener.written(i);
        return;
      }
    });
  };
  
  public void disconnect(){
    disconnected = true;
    rch = null;
  };

}
