package task4;

import java.io.IOException;

import task1.*;

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

  public int read(byte[] bytes, int offset, int length) throws IOException{
    if (disconnected){
      throw new IOException("channel is disconnected");
    }
    else if (rch.disconnected){
      throw new IOException("channel is dangling");
    }

    if (out.empty()){
      synchronized (this) {
        try{wait(1000);}catch(InterruptedException e){};
      }
    }

    int i = 0;
    synchronized (this){
      while(i < length) { 
        try{
            bytes[offset + i] = out.pull();
            notifyAll();
            i += 1;
        } catch (IllegalStateException e) {
            return i;
        }
      }
    }
    return i;
  };

  public int write(byte[] bytes, int offset, int length) throws IOException {
    if (disconnected){
      throw new IOException("channel is disconnected");
    }
    else if (rch.disconnected){
      throw new IOException("channel is dangling");
    }

    if (in.full()){
      synchronized (this) {
        try{wait(1000);}catch(InterruptedException e){};
      }
    }

    int i = 0;
    synchronized (this){
      while(i < length) { 
        try{
          in.push(bytes[offset + i]);
          notifyAll();
          i += 1;
        } catch (IllegalStateException e) {
            return i;
        }
      }
    }
    return i;


  };
  
  public void disconnect(){
    disconnected = true;
    rch = null;
  };

}
