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
      return 0;
    }

    int i = 0;
    while(offset + i < length) { 
      try{
          bytes[offset + i] = out.pull();
          i += 1;
      } catch (IllegalStateException e) {
          return i;
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
      return 0;
    }

    int i = 0;
    while(offset + i < length) { 
      try{
        in.push(bytes[offset + i]);
        i += 1;
      } catch (IllegalStateException e) {
          return i;
      }
    }
    return i;
  };
  
  public void disconnect(){
    disconnected = true;
    rch = null;
  };

}
