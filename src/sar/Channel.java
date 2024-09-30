package sar;

import java.io.IOException;

public class Channel {
  
  ChannelImp in;
  ChannelImp out;
  Channel rch;
  Boolean disconnected;

  public static final int CAPACITY = 5;

  Channel (){
    in = new ChannelImp(Channel.CAPACITY);
    out = new ChannelImp(Channel.CAPACITY);
  };

  public synchronized int read(byte[] bytes, int offset, int length) throws IOException{
    if (disconnected){
      throw new IOException("channel is disconnected");
    }
    else if (rch.disconnected){
      throw new IOException("channel is dangling");
    }
    return out.read(bytes, offset, length);
  };

  public synchronized int write(byte[] bytes, int offset, int length) throws IOException {
    if (disconnected){
      throw new IOException("channel is disconnected");
    }
    else if (rch.disconnected){
      throw new IOException("channel is dangling");
    }
    return in.write(bytes, offset, length);
  };
  
  void disconnect(){
    disconnected = true;
  };

}
