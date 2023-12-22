import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;


public class Main {
    //8kB = 2^13 = 8192
    //32kB = 2^15 = 32768
    //1MB = 2^20 = 1048576
    //32MB = 2^25 = 33554432
    private static final int BUFFER_SIZE = 1048576;

    static Map<Byte, Long> getFreq(String filePath) throws IOException{
        Map<Byte, Long> freq = new HashMap<>();
//        long[] freqArr = new long[256];
        InputStream inStream = new BufferedInputStream(
                new FileInputStream(filePath)
        );
        int bytesRead = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytesRead = 0;
        while((bytesRead = inStream.read(buffer)) != -1){
            for(int i = 0; i < bytesRead; i++){
                freq.compute(
                        buffer[i],
                        (key, oldValue) -> oldValue==null?1:oldValue+1
                );
            }
            totalBytesRead += bytesRead;
        }
        inStream.close();
        return freq;
    }

    static TreeNode<Byte> buildTree(Map<Byte, Long> freq){
        PriorityQueue<TreeNode<Byte>> pq = new PriorityQueue<>(
                (o1, o2) -> (int) (o1.val - o2.val)
        );
        freq.forEach((k,v)->
                pq.add(new TreeNode<>(k, v, null, null)));
        while(pq.size() > 1){
            TreeNode<Byte> a = pq.poll();
            TreeNode<Byte> b = pq.poll();
            pq.add(a.mergeWith(b, null));
        }
        return pq.poll();
    }

    static void go(
            TreeNode<Byte> tree,
            Map<Byte, ArrayList<Character>> codes,
            ArrayList<Character> code
    ){
        if(tree.left == tree.right){
            codes.put(tree.key, new ArrayList<>(code));
            return;
        }
        code.add('0');
        go(tree.left, codes, code);
        code.set(code.size()-1, '1');
        go(tree.right, codes, code);
        code.remove(code.size()-1);
    }

    static Map<Byte, ArrayList<Character>> getCodes(TreeNode<Byte> tree){
        Map<Byte, ArrayList<Character>> codes = new HashMap<>();
        ArrayList<Character> code = new ArrayList<>();
        go(tree, codes, code);
        return codes;
    }

    static byte[] serialize(long val){
        byte[] arr = new byte[8];
        long bitmask = 0x00000000000000ffL;
        //Least significant byte
        arr[7] = (byte)(val & bitmask);
        arr[6] = (byte)((val >> 8) & bitmask);
        arr[5] = (byte)((val >> 16) & bitmask);
        arr[4] = (byte)((val >> 24) & bitmask);
        arr[3] = (byte)((val >> 32) & bitmask);
        arr[2] = (byte)((val >> 40) & bitmask);
        arr[1] = (byte)((val >> 48) & bitmask);
        //Most significant byte
        //IGNORE SIGN BIT
        arr[0] = (byte)((val >> 56) & bitmask);
        return arr;
    }

    static byte[] serialize(int val){
        byte[] arr = new byte[4];
        int bitmask = 0x000000ff;
        //least significant byte
        arr[3] = (byte)(val & bitmask);
        arr[2] = (byte)((val >> 8) & bitmask);
        arr[1] = (byte)((val >> 16) & bitmask);
        //most significant byte
        arr[0] = (byte)((val >> 24) & bitmask);
        return arr;
    }

    static byte[] serializeKeys(Map<Byte, ArrayList<Character>> codeMap){
        byte[] arr = new byte[codeMap.size()];
        int i = 0;
        for(Byte b: codeMap.keySet()){arr[i++] = b;}
        return arr;
    }

    static byte[] serializeCodeLengths(Map<Byte, ArrayList<Character>> codeMap){
        byte[] arr = new byte[codeMap.size()];
        int i = 0;
        for(Byte b: codeMap.keySet()){
            arr[i++] = (byte)codeMap.get(b).size();
        }
        return arr;
    }
    static byte[] serializeCodes(Map<Byte, ArrayList<Character>> codes){
        ArrayList<Byte> byteList = new ArrayList<>();
        byteList.add((byte)0);
        //points to bits in a byte
        int i = 7;
        //will be appended to result
        Byte currByte = byteList.get(0);
        for(Byte b: codes.keySet()){
            ArrayList<Character> code = codes.get(b);
            //points to code chars
            int j = 0;
            while(j < code.size()){
                if(i < 0){
                    byteList.set(byteList.size()-1, currByte);
                    byteList.add((byte)0);
                    currByte = byteList.get(byteList.size()-1);
                    i = 7;
                }
                currByte = (byte) (currByte
                        |((Integer.parseInt(""+code.get(j))) << i));
                i--; j++;
            }
        }
        if(i < 7){byteList.set(byteList.size()-1, currByte);}
        byte[] byteArr = new byte[byteList.size()];
        i = 0;
        for(Byte b: byteList){
            byteArr[i] = b;
            i++;
        }
        return byteArr;
    }

    static void writeEncodedBody(
            Map<Byte, ArrayList<Character>> codes,
            OutputStream outputStream,
            String path
    ) throws IOException {
        byte[] inBuffer = new byte[BUFFER_SIZE];
        byte[] outBuffer = new byte[BUFFER_SIZE];
        InputStream inputStream = new BufferedInputStream(
                new FileInputStream(path)
        );
        int outBufferBitIndex = 0, inBufferIndex, bytesRead;
        //get chunks till end of file
        while((bytesRead = inputStream.read(inBuffer)) != -1){
//            System.out.println("Bytes read = "+bytesRead);
            inBufferIndex = 0;
            //for each chunk
            while(inBufferIndex < bytesRead){
                //get code of current byte
//                System.out.println("Current byte: "+inBuffer[inBufferIndex]);
                ArrayList<Character> code = codes.get(inBuffer[inBufferIndex]);
                //write code in compressed file
                for(Character c: code){
                    outBuffer[outBufferBitIndex / 8] |=
                            ((c == '1')? 1 : 0) << (7 - (outBufferBitIndex % 8));
                    outBufferBitIndex++;
                    //write outBuffer to disk once got full
                    if(outBufferBitIndex/8 >= outBuffer.length){
//                        System.out.println("outBuffer full!!!!");
                        outputStream.write(outBuffer);
                        outBufferBitIndex = 0;
//                        System.out.println(Arrays.toString(outBuffer));
                        Arrays.fill(outBuffer, (byte)0);
                    }
                }
                inBufferIndex++;
            }
        }

//        System.out.println("outBufferBitIndex: "+outBufferBitIndex);
//        System.out.println(Arrays.toString(outBuffer));
        //write the remaining content in outBuffer
        //after reading the whole file
        if(outBufferBitIndex != 0){
            outputStream.write(outBuffer, 0, (outBufferBitIndex/8)+1);
        }
    }
    static long deserializeLong(byte[] bytes){
        return ByteBuffer.wrap(bytes).getLong();
    }

    static int deserializeInt(byte[] bytes){
        return ByteBuffer.wrap(bytes).getInt();
    }

    static long getOriginalBytesCount(InputStream inputStream) throws IOException{
        byte[] countBytes = inputStream.readNBytes(8);
        return deserializeLong(countBytes);
    }

    static int getUniqueCount(InputStream inputStream) throws IOException {
        byte[] countBytes = inputStream.readNBytes(4);
        return deserializeInt(countBytes);
    }

    static byte[] getUniqueValues(InputStream inputStream, int uniqueCount)
            throws IOException{
        //number of unique values will not exceed max array size
        //in java, given that max file size = 3GB (for any n)
        return inputStream.readNBytes(uniqueCount);
    }

    static byte[] getCodesLengths(InputStream inputStream, int uniqueCount) throws IOException {
        byte[] lengths = new byte[uniqueCount];
        inputStream.readNBytes(lengths, 0, uniqueCount);
        return lengths;
    }

    static String[] getCodes(
            InputStream inputStream,
            int uniqueCount,
            byte[] lengths
    ) throws IOException{
        long totalNumberOfBits = 0;
        for(byte b: lengths){totalNumberOfBits += b;}
        //total number of bytes for the codes section in header
        //recall that codes have variable lengths
        int byteCount = (int)Math.ceilDiv(totalNumberOfBits, 8);
        byte[] buffer = new byte[byteCount];
        inputStream.read(buffer, 0, byteCount);
//        System.out.println(Arrays.toString(buffer));
        long bufferBitIndex = 0;
        String[] codes = new String[uniqueCount];
        for(int i = 0; i < lengths.length; i++){
            int codeLength = lengths[i];
            codes[i] = "";
            for(int j = 0; j < codeLength; j++){
                int currBit = (buffer[(int)((bufferBitIndex+j)/8)] &
                        (1 << (7-(bufferBitIndex+j)%8)));
//                System.out.println("currBit = "+currBit);
                codes[i] += (currBit != 0)?"1":"0";
            }
            bufferBitIndex += codeLength;
        }
        return codes;
    }

    static void decodeFile(
            InputStream inputStream,
            OutputStream outputStream,
            String[] codes,
            byte[] uniqueValues,
            long originalBytesCount
    ) throws IOException{
        Map<String, Byte> decoderMap = new HashMap<>();
        for(int i = 0; i < codes.length; i++){
            decoderMap.put(codes[i], uniqueValues[i]);
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        byte[] outBuffer = new byte[BUFFER_SIZE];
        int bytesRead;
        int outBufferIndex = 0;
        StringBuilder currKeyBuilder = new StringBuilder();
        while((bytesRead =
                inputStream.read(buffer, 0, BUFFER_SIZE)) != -1){
            long bufferBitIndex = 0;
            while(originalBytesCount > 0
                    && bufferBitIndex/8 < buffer.length){
                int currBit = buffer[(int)(bufferBitIndex/8)]
                        & (1 << (7-bufferBitIndex%8));
                currKeyBuilder.append((currBit != 0)? 1:0);
                Byte value = decoderMap.get(currKeyBuilder.toString());
//                System.out.println("key = "+currKeyBuilder);
//                System.out.println("value = "+value);
                if(value != null){
                    outBuffer[outBufferIndex++] = value;
                    currKeyBuilder = new StringBuilder();
                    originalBytesCount--;
                }
                bufferBitIndex++;
                if(outBufferIndex >= outBuffer.length){
                    outputStream.write(outBuffer);
                    outBufferIndex = 0;
                    Arrays.fill(outBuffer, (byte)0);
                }
            }
            if(originalBytesCount == 0){
                outputStream.write(outBuffer, 0, outBufferIndex);
            }
            Arrays.fill(buffer, (byte)0);
        }
    }

    static void decompressFile(String inPath, String outPath) throws IOException{
        InputStream inputStream = new BufferedInputStream(
                new FileInputStream(inPath)
        );
        OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(outPath)
        );
        //to avoid decoding padding
        long originalBytesCount = getOriginalBytesCount(inputStream);
        System.out.println("original # of bytes: "+originalBytesCount);
        //number of unique values
        int uniqueValuesCount = getUniqueCount(inputStream);
        //unique values themselves
        byte[] uniqueValues = getUniqueValues(inputStream, uniqueValuesCount);
        //lengths of codes for each unique value
        byte[] codesLengths = getCodesLengths(inputStream, uniqueValuesCount);

        String[] codes = getCodes(inputStream, uniqueValuesCount, codesLengths);

        decodeFile(inputStream, outputStream, codes, uniqueValues, originalBytesCount);

        inputStream.close();
        outputStream.close();
    }

    public static void main(String[] args) {
        //\n: line feed, \r: carriage return
        //"C:\\Users\\3arrows\\Downloads\\gbbct10.seq\\gbbct10.seq"
        //"C:\\Users\\3arrows\\Downloads\\ubuntu-18.04.6-desktop-amd64.iso"
        //"D:\\activity_tests\\test1.in"
        //"D:\\JavaProjects\\HuffmanAlgorithm\\src\\abc.txt"
        //"C:\\Users\\3arrows\\Downloads\\Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf"
        String path = "D:\\JavaProjects\\HuffmanAlgorithm\\src\\abc.txt";


        long time = System.currentTimeMillis();
        Map<Byte, Long> freq = new HashMap<>();
        try{
            freq = getFreq(path);
        }catch (IOException e){
            e.printStackTrace();
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Freq took "+ time +" ms");
//        freq.forEach((k,v)-> System.out.println(Character.toString(k)+", "+k+": "+v));

        System.out.println(freq.size());
//        freq.forEach((k,v)-> System.out.println(k+": "+v));

        time = System.currentTimeMillis();
        TreeNode<Byte> tree = buildTree(freq);
        time = System.currentTimeMillis() - time;
        System.out.println("Building tree took "+time+" ms");
//        tree.bfs
        time = System.currentTimeMillis();
        Map<Byte, ArrayList<Character>> codes = getCodes(tree);
        time = System.currentTimeMillis() - time;
        System.out.println("Getting codes took "+time+" ms");
//        codes.forEach((k,v)-> System.out.println(Character.toString(k)+", "+k+": "+v));
//        codes.forEach((k,v)-> System.out.println(k+": "+v));

//        System.out.println(Arrays.toString(serializeCodes(codes)));
        time = System.currentTimeMillis();

        byte[] originalBytesCount = serialize(
                freq.values().stream().reduce(0L, Long::sum)
        );
        System.out.println(Arrays.toString(originalBytesCount));
        byte[] uniqueValuesCount = serialize(freq.size());
        byte[] serializedKeys = serializeKeys(codes);
        byte[] lengths = serializeCodeLengths(codes);
        byte[] byteCode = serializeCodes(codes);
        time = System.currentTimeMillis() - time;
        System.out.println("Header serialization took "+time+" ms");


        try{
            time = System.currentTimeMillis();
            OutputStream outputStream = new BufferedOutputStream(
                    new FileOutputStream("test.bin")
            );
            //to ignore padding at end of bitString
            outputStream.write(originalBytesCount, 0, originalBytesCount.length);
            outputStream.write(uniqueValuesCount, 0, uniqueValuesCount.length);
            outputStream.write(serializedKeys, 0, codes.size());
            outputStream.write(lengths, 0, lengths.length);
            outputStream.write(byteCode, 0, byteCode.length);
            time = System.currentTimeMillis() - time;
            System.out.println("Header writing took "+time+" ms");

            time = System.currentTimeMillis();
            writeEncodedBody(codes, outputStream, path);
            time = System.currentTimeMillis() - time;
            System.out.println("Body writing took "+time+" ms");
            outputStream.close();


            decompressFile("test.bin", "d_test.bin");

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}