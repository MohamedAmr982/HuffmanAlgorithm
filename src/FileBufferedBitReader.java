import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class FileBufferedBitReader {

    private int size;
    private InputStream inputStream;
    private byte[] buffer;
    private long bufferBitIndex;
    private int bytesLoaded;

    public FileBufferedBitReader(int size, InputStream inputStream){
        this.size = size;
        this.inputStream = inputStream;
        this.buffer = new byte[this.size];
        this.bufferBitIndex = 0;
        this.bytesLoaded = 0;
    }

    private void load() throws IOException{
        Arrays.fill(buffer, (byte)0);
        bytesLoaded = inputStream.read(buffer);
        bufferBitIndex = 0;
    }

    public boolean hasNext() throws IOException{
        if((int)(bufferBitIndex/8) < bytesLoaded){return true;}
        load();
        return bytesLoaded != -1;
    }

    public int getNext() throws IOException{
        if(!hasNext()) return -1;
        int currBit = buffer[(int)(bufferBitIndex/8)]
                & 1 << (7-(bufferBitIndex % 8));
        bufferBitIndex++;
        return (currBit != 0)? 1 : 0;
    }

}
