import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StepBufferedByteReader {
    private int size;
    private InputStream inputStream;
    private int step;
    private byte[] buffer;
    private int bufferIndex;
    private int bytesLoaded;

    public StepBufferedByteReader(int size, InputStream inputStream, int step){
        this.size = size;
        this.inputStream = inputStream;
        this.step = step;
        this.buffer = new byte[this.size];
        this.bufferIndex = 0;
        this.bytesLoaded = 0;
    }

    private void load() throws IOException {
        Arrays.fill(buffer, (byte)0);
        bytesLoaded = inputStream.read(buffer);
        bufferIndex = 0;
    }

    public boolean hasNext() throws IOException{
        if(bufferIndex < bytesLoaded){return true;}
        load();
        return bytesLoaded != -1;
    }

    private void go(int i, List<Byte> list) throws IOException{
        //case: i finishes but still not file end
        //case: i not finished but file end
        //case: both file end and i finished
        if(i >= step || !hasNext()){return;}
        list.add(buffer[bufferIndex++]);
        go(i+1, list);
    }

    public List<Byte> getNext() throws IOException {
        List<Byte> list = new ArrayList<>();
        go(0, list);
        return list;
    }


}
