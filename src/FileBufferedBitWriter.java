import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class BufferedBitWriter {
    private int size;
    private OutputStream outputStream;
    private long bufferBitIndex;
    private byte[] buffer;

    public BufferedBitWriter(int size, OutputStream outputStream){
        this.size = size;
        this.buffer = new byte[this.size];
        this.outputStream = outputStream;
        this.bufferBitIndex = 0;
    }

    public void writeBit(int bit) throws IOException {
        //writes to outputStream once the last bit is filled
        //IE last bit comes first then buffer is written
        buffer[(int)(bufferBitIndex/8)] |= bit << (7 - bufferBitIndex % 8);
        bufferBitIndex++;
        if(bufferBitIndex/8 >= buffer.length){flush();}
    }
    public void flush() throws IOException{
        if(bufferBitIndex != 0){
            outputStream.write(buffer, 0, (int)(bufferBitIndex/8)+1);
            bufferBitIndex = 0;
            Arrays.fill(buffer, (byte)0);
        }
    }
}
