import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class TreeNode<T>{
    T key;
    long val;
    TreeNode<T> left;
    TreeNode<T> right;
    public TreeNode(T key, long val, TreeNode<T> left, TreeNode<T> right){
        this.key = key;
        this.val = val;
        this.left = left;
        this.right = right;
    }
    public TreeNode<T> min(TreeNode<T> b){
        return (this.val <= b.val)? this : b;
    }
    public TreeNode<T> max(TreeNode<T> b){
        return (this.val > b.val)? this : b;
    }
    public TreeNode<T> mergeWith(TreeNode<T> t, T newKey){
        return new TreeNode<>(
                newKey,
                this.val + t.val,
                this.min(t),
                this.max(t)
        );
    }
    @Override
    public String toString(){
        if(key == null){
            return "{ - : "+this.val+"}";
        }
        return "{" + this.key.toString() + ": " + this.val + "}";
    }
    public void bfs(){
        Queue<TreeNode<T>> q = new LinkedList<>();
        q.add(this);
        while(!q.isEmpty()){
            TreeNode<T> curr = q.poll();
            if(curr.left != null){q.add(curr.left);}
            if(curr.right != null){q.add(curr.right);}
//            System.out.println(curr);
        }
    }

}

class BytesWrapper{
    public byte[] bytes;

    public BytesWrapper(byte[] bytes){
        this.bytes = bytes;
    }

    @Override
    public boolean equals(Object o){
        if(o.getClass() != BytesWrapper.class){return false;}
        BytesWrapper wrapper = (BytesWrapper) o;
        if(wrapper.bytes.length != this.bytes.length){return false;}
        for(int i = 0; i < this.bytes.length; i++){
            if(bytes[i] != wrapper.bytes[i]){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode(){
        int code = 0;
        final int basePrime = 877;
        final int moduloPrime = 27644437;
        for(int i = 0; i < this.bytes.length; i++){
            code = (basePrime * code + bytes[i]) % moduloPrime;
        }
        return code;
//        return 1;
    }
}

public class Main {
    //8kB = 2^13 = 8192
    //32kB = 2^15 = 32768
    //1MB = 2^20 = 1048576
    //32MB = 2^25 = 33554432
    private static final int BUFFER_SIZE = 8192;

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
                //main overhead!!!!!!!!
//                if(!freq.containsKey(buffer[i])){
//                    freq.put(buffer[i], 1L);
//                }else{
//                    freq.put(buffer[i], freq.get(buffer[i])+1);
//                }
                //fast but not memory efficient
//                freqArr[buffer[i]]++;
                //little improvement
                freq.compute(
                        buffer[i],
                        (key, oldValue) -> oldValue==null?1:oldValue+1
                );
            }
            totalBytesRead += bytesRead;
        }
//            System.out.println("Total bytes read: "+ totalBytesRead);
        inStream.close();
//        for(int i = 0; i < freqArr.length; i++){
//            if(freqArr[i] != 0){
//                freq.put((byte)i, freqArr[i]);
//            }
//        }

        return freq;
    }

    static Map<BytesWrapper, Long> getFreq2(String filePath) throws IOException{
        Map<BytesWrapper, Long> freq = new HashMap<>();
        InputStream inStream = new BufferedInputStream(
                new FileInputStream(filePath)
        );
        int bytesRead = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytesRead = 0;
        byte[] barr = new byte[1];
        BytesWrapper wrapper = new BytesWrapper(barr);
        while((bytesRead = inStream.read(buffer)) != -1){
            for(int i = 0; i < bytesRead; i++){
                barr[0] = buffer[i];
                freq.compute(
                        wrapper,
                        (key, oldValue) -> oldValue==null?1:oldValue+1
                );
            }
            totalBytesRead += bytesRead;
        }
        inStream.close();
        return freq;
    }

    static void getFreq3(String filePath) throws IOException{
        Set<BytesWrapper> freq = new HashSet<>();
        InputStream inStream = new BufferedInputStream(
                new FileInputStream(filePath)
        );
        int bytesRead = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytesRead = 0;
        while((bytesRead = inStream.read(buffer)) != -1){
            for(int i = 0; i < bytesRead; i++){
                byte[] barr = new byte[1];
                BytesWrapper wrapper = new BytesWrapper(barr);
                barr[0] = buffer[i];
                freq.add(wrapper);
            }
            totalBytesRead += bytesRead;
        }
        inStream.close();
        freq.forEach((k)-> System.out.println(k.bytes[0]));
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
        //Least significant byte
        arr[7] = (byte)(val & 0x00000000000000ffL);
        arr[6] = (byte)(val & 0x000000000000ff00L);
        arr[5] = (byte)(val & 0x0000000000ff0000L);
        arr[4] = (byte)(val & 0x00000000ff000000L);
        arr[3] = (byte)(val & 0x000000ff00000000L);
        arr[2] = (byte)(val & 0x0000ff0000000000L);
        arr[1] = (byte)(val & 0x00ff000000000000L);
        //most significant byte
        arr[0] = (byte)(val & 0xff00000000000000L);
        return arr;
    }

    static byte[] serializeKeys(Map<Byte, ArrayList<Character>> codeMap){
        byte[] arr = new byte[codeMap.size()];
        int i = 0;
        for(Byte b: codeMap.keySet()){arr[i++] = b;}
        return arr;
    }

    static byte[][] serializeCodeLengths(Map<Byte, ArrayList<Character>> codeMap){
        byte[][] arr = new byte[codeMap.size()][8];
        int i = 0;
        for(Byte b: codeMap.keySet()){
            arr[i++] = serialize(codeMap.get(b).size());
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
        long l = 0;
        l |= (long)bytes[0] << 56;
        l |= (long)bytes[1] << 48;
        l |= (long)bytes[2] << 40;
        l |= (long)bytes[3] << 32;
        l |= (long)bytes[4] << 24;
        l |= (long)bytes[5] << 16;
        l |= (long)bytes[6] << 8;
        l |= bytes[7];
        return l;
    }

    static long getUniqueCount(InputStream inputStream) throws IOException {
        byte[] countBytes = inputStream.readNBytes(8);
        return deserializeLong(countBytes);
    }

    static byte[] getUniqueValues(InputStream inputStream, long uniqueCount)
            throws IOException{
        //max 256 unique values --> will not exceed int size (max arr length)
        return inputStream.readNBytes((int)uniqueCount);
    }

//    static byte[] getCodesLengths(InputStream inputStream, long uniqueCount){
//
//    }

    public static void main(String[] args) {
        //\n: line feed, \r: carriage return
        //"C:\\Users\\3arrows\\Downloads\\gbbct10.seq\\gbbct10.seq"
        //"C:\\Users\\3arrows\\Downloads\\ubuntu-18.04.6-desktop-amd64.iso"
        //"D:\\activity_tests\\test1.in"
        //"D:\\JavaProjects\\HuffmanAlgorithm\\src\\abc.txt"
        //"C:\\Users\\3arrows\\Downloads\\Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf"
        String path = "C:\\Users\\3arrows\\Downloads\\gbbct10.seq\\gbbct10.seq";

//        long time = System.currentTimeMillis();
//        try{
//            getFreq3(path);
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//        time = System.currentTimeMillis() - time;
//        System.out.println("Took "+time+" ms");

//        long time = System.currentTimeMillis();
//        Map<BytesWrapper, Long> freq = new HashMap<>();
//        try{
//            freq = getFreq2(path);
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//        time = System.currentTimeMillis() - time;
//        System.out.println("Took "+time+" ms");
//        freq.forEach((k,v)-> System.out.println(k.bytes[0] + ": " + v));


        long time = System.currentTimeMillis();
        Map<Byte, Long> freq = new HashMap<>();
        try{
            freq = getFreq(path);
        }catch (IOException e){
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Freq took "+ time +" ms");
        freq.forEach((k,v)-> System.out.println(Character.toString(k)+", "+k+": "+v));
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

//        System.out.println(Arrays.toString(serializeCodes(codes)));
        time = System.currentTimeMillis();
        byte[] uniqueValuesCount = serialize(freq.size());
        byte[] serializedKeys = serializeKeys(codes);
        byte[][] lengths =serializeCodeLengths(codes);
        byte[] byteCode = serializeCodes(codes);
        time = System.currentTimeMillis() - time;
        System.out.println("Header serialization took "+time+" ms");


        try{
            time = System.currentTimeMillis();
            OutputStream outputStream = new BufferedOutputStream(
                    new FileOutputStream("test.bin")
            );
            outputStream.write(uniqueValuesCount, 0, 8);
            outputStream.write(serializedKeys, 0, codes.size());
            for(byte[] barr: lengths){
                outputStream.write(barr, 0, 8);
            }
            outputStream.write(byteCode, 0, byteCode.length);
            time = System.currentTimeMillis() - time;
            System.out.println("Header writing took "+time+" ms");

            time = System.currentTimeMillis();
            writeEncodedBody(codes, outputStream, path);
            time = System.currentTimeMillis() - time;
            System.out.println("Body writing took "+time+" ms");
            outputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}