public class BitStringReader {
    private byte[] buffer;
    private long bufferBitIndex;

    public BitStringReader(byte[] buffer){
        this.buffer = buffer;
        this.bufferBitIndex = 0;
    }

    public StringBuilder getNextCode(int length){
        //length in BITS
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < length; i++){
            int currBit = buffer[(int)(bufferBitIndex/8)] &
                    (1 << (7-bufferBitIndex%8));
            stringBuilder.append((currBit != 0)? '1':'0');
            bufferBitIndex++;
        }
        return stringBuilder;
    }
}
