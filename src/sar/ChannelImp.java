package sar;

public class ChannelImp {

    CircularBuffer buffer;
    
    ChannelImp(int capacity){
        buffer = new CircularBuffer(capacity);
    }
    
    public int write(byte[] bytes, int offset, int length){
        int i = 0;
        while(i < length) { 
        try{
            buffer.push(bytes[offset + i]);
        } catch (IllegalStateException e) {
            break;
        }
        i += 1;
        }
        return i;
    }
    
    public int read(byte[] bytes, int offset, int length){
        int i = 0;
        while(i < length) { 
        try{
            bytes[offset + i] = buffer.pull();
        } catch (IllegalStateException e) {
            break;
        }
        i += 1;
        }
        return i;
    }


}
