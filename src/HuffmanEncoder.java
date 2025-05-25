import java.io.*;
import java.util.*;

public class HuffmanEncoder {
    // input word size
    private final int n;
    private String filePath;
    private String outFilePath;
    // 8kB = 2^13 = 8192
    // 32kB = 2^15 = 32768
    // 512kB = 2^19 = 524288
    private static final int BUFFER_SIZE = 8192;

    public HuffmanEncoder(int n, String filePath){
        this.n = n;
        this.filePath = filePath;
        this.outFilePath = HuffmanUtility.getFilePath(this.filePath)
                +System.getProperty("file.separator")
                +this.n
                +"."
                +HuffmanUtility.getFileName(this.filePath)
                +".hc";
    }

    private Map<List<Byte>, Long> getFreq() throws IOException{
        Map<List<Byte>, Long> freq = new HashMap<>();
        InputStream inputStream = new BufferedInputStream(
                new FileInputStream(filePath)
        );
        int bytesRead = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        List<Byte> currKey = new ArrayList<>();
        while((bytesRead = inputStream.read(buffer)) != -1){
            for(int i = 0; i < bytesRead; i++){
                currKey.add(buffer[i]);
                if(currKey.size() == n){
                    freq.compute(
                            currKey,
                            (key, oldValue) -> (oldValue == null)? 1: oldValue+1
                    );
                    currKey = new ArrayList<>();
                }
            }
        }
        if(!currKey.isEmpty()){
            freq.compute(
                    currKey,
                    (key, oldValue) -> (oldValue == null)? 1: oldValue+1
            );
        }
        inputStream.close();
        return freq;
    }

    private TreeNode<List<Byte>> buildTree(Map<List<Byte>, Long> freq){
        // min heap
        PriorityQueue<TreeNode<List<Byte>>> pq = new PriorityQueue<>(
                (o1, o2) -> (int) (o1.val - o2.val)
        );
        freq.forEach(
                (k,v) -> pq.add(new TreeNode<>(k, v, null, null))
        );
        while(pq.size() > 1){
            var a = pq.poll();
            var b = pq.poll();
            pq.add(a.mergeWith(b, null));
        }
        return pq.poll();
    }

    private void assignCodesToTreeNodes(
            TreeNode<List<Byte>> tree,
            Map<List<Byte>, StringBuilder> codes,
            StringBuilder code
    ){
        if(tree.left == tree.right){
            // leaf
            codes.put(tree.key, new StringBuilder(code));
            return;
        }
        code.append('0');
        assignCodesToTreeNodes(tree.left, codes, code);
        code.replace(code.length()-1, code.length(), "1");
        assignCodesToTreeNodes(tree.right, codes, code);
        code.delete(code.length()-1, code.length());
    }

    private Map<List<Byte>, StringBuilder> getCodes(TreeNode<List<Byte>> tree){
        Map<List<Byte>, StringBuilder> codes = new HashMap<>();
        StringBuilder code = new StringBuilder();
        assignCodesToTreeNodes(tree, codes, code);
        return codes;
    }

    private void writeEncodedBody(
            Map<List<Byte>, StringBuilder> codes,
            OutputStream outputStream
    ) throws IOException{
        InputStream inputStream = new BufferedInputStream(
                new FileInputStream(this.filePath)
        );
        StepBufferedByteReader reader =
                new StepBufferedByteReader(BUFFER_SIZE, inputStream, n);
        FileBufferedBitWriter writer =
                new FileBufferedBitWriter(BUFFER_SIZE, outputStream);
        List<Byte> byteList;
        while(reader.hasNext()){
            byteList = reader.getNext();
            StringBuilder stringBuilder = codes.get(byteList);
            for(int i = 0; i < stringBuilder.length(); i++){
                writer.writeBit((stringBuilder.charAt(i) == '1')? 1:0);
            }
        }
        writer.flush();
        inputStream.close();
    }

    public void compressFile() throws IOException {
        var freq = getFreq();
        var tree = buildTree(freq);
        Map<List<Byte>, StringBuilder> codes;
        if(freq.size() == 1){
            codes = new HashMap<>();
            codes.put(tree.key, new StringBuilder("0"));
        }else{
            codes = getCodes(tree);
        }

//        codes.forEach((k,v)-> System.out.println(k+": "+v));

        var codesList =
                HuffmanUtility.getCodesList(n, codes);

        OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(getOutFilePath())
        );

        //n
        byte[] serializedN = HuffmanUtility.serializeInt(n);
        outputStream.write(serializedN, 0, serializedN.length);
        //number of bytes in original file (long)
        long byteCount = freq.entrySet().stream().reduce(0L,
                (l,e)->l + e.getKey().size()*e.getValue(),
                Long::sum
        );
        byte[] serializedByteCount = HuffmanUtility.serializeLong(byteCount);
        outputStream.write(serializedByteCount, 0, serializedByteCount.length);
        //number of unique values
        byte[] serializedUniqueValuesCount =
                HuffmanUtility.serializeInt(codesList.size());
        outputStream.write(serializedUniqueValuesCount, 0,
                serializedUniqueValuesCount.length);
        //length of last key
        //can be less than n, or EQUAL to n
        int lastKeyLength = codesList.get(codesList.size()-1).getKey().size();
        byte[] serializedRemKeyLen = HuffmanUtility.serializeInt(lastKeyLength);
        outputStream.write(serializedRemKeyLen, 0, serializedRemKeyLen.length);
        //unique values themselves
        //keySize: to avoid extra 0 bytes in case n > number of bytes in file
        int keySize = (n > byteCount)? codesList.get(0).getKey().size() : n;
        byte[][] serializedKeys = HuffmanUtility.serializeKeys(keySize, codesList);
        for(int i = 0; i < serializedKeys.length-1; i++){
            outputStream.write(serializedKeys[i], 0, serializedKeys[i].length);
        }
        outputStream.write(serializedKeys[serializedKeys.length-1], 0, lastKeyLength);
        //code lengths
        byte[] serializedCodeLengths = HuffmanUtility.serializeCodeLengths(codesList);
        outputStream.write(serializedCodeLengths, 0, serializedCodeLengths.length);
        //codes as a single bitString
        byte[] codeString = HuffmanUtility.serializeCodes(codesList);
        outputStream.write(codeString, 0, codeString.length);

        //encoding data
        writeEncodedBody(codes, outputStream);

        outputStream.close();
    }

    public String getOutFilePath(){
        return this.outFilePath;
    }

    public float getCompressionRatio(){
        return HuffmanUtility.getFileSize(this.outFilePath) * 1.0f
                / HuffmanUtility.getFileSize(this.filePath);
    }
}
