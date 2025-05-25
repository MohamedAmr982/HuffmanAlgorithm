import java.util.ArrayList;
import java.util.List;

public class ArrayBitWriter {
    private List<Byte> byteList;
    private byte currByte;
    private int bitIndex;

    public ArrayBitWriter(){
        this.byteList = new ArrayList<>();
        bitIndex = 7;
        currByte = 0;
    }

    public void writeBit(int bit){
        currByte |= (bit << bitIndex);
        bitIndex--;
        if(bitIndex < 0){flush();}
    }

    public void flush(){
        //currByte can be flushed partially, but
        // padding bits are also written
        if(bitIndex != 7){
            byteList.add(currByte);
            bitIndex = 7;
            currByte = 0;
        }
    }

    public byte[] get(){
        flush();
        byte[] byteArr = new byte[this.byteList.size()];
        for(int i = 0; i < byteArr.length; i++){
            byteArr[i] = byteList.get(i);
        }
        return byteArr;
    }


}
