import java.io.*;
import java.util.*;

public class HuffmanDecoder {
    // input word size
    private int n;
    private String filePath;
    private String outFilePath;
    // 8kB = 2^13 = 8192
    // 32kB = 2^15 = 32768
    // 512kB = 2^19 = 524288
    private static final int BUFFER_SIZE = 1024;

    public HuffmanDecoder(String filePath){
        this.filePath = filePath;

        this.outFilePath = HuffmanUtility.getFilePath(this.filePath)
                +System.getProperty("file.separator")
                +"extracted."
                +HuffmanUtility.getFileName(this.filePath)
                .replaceAll(".hc", "");
    }

    private void getN(InputStream inputStream) throws IOException {
        this.n =  HuffmanUtility.deserializeInt(
                inputStream.readNBytes(4)
        );
    }

    private long getOriginalFileSize(InputStream inputStream) throws IOException{
        return HuffmanUtility.deserializeLong(
                inputStream.readNBytes(8)
        );
    }

    private int getUniqueValuesCount(InputStream inputStream) throws IOException{
        return HuffmanUtility.deserializeInt(
                inputStream.readNBytes(4)
        );
    }

    private int getSizeOfLastKey(InputStream inputStream) throws IOException{
        return HuffmanUtility.deserializeInt(
                inputStream.readNBytes(4)
        );
    }

    private List<List<Byte>> getUniqueValues(
            InputStream inputStream,
            int uniqueValuesCount,
            int sizeofLastKey
    ) throws IOException{
        List<List<Byte>> uniqueValuesList = new ArrayList<>(uniqueValuesCount);
        byte[] uniqueValuesBytes =
                new byte[n * (uniqueValuesCount - 1) + sizeofLastKey];
        inputStream.readNBytes(uniqueValuesBytes, 0, uniqueValuesBytes.length);
        int i = 0;
        while(i < n * (uniqueValuesCount - 1)){
            List<Byte> valueBytesList = new ArrayList<>(n);
            for(int j = 0; j < n; j++){
                valueBytesList.add(uniqueValuesBytes[i+j]);
            }
            uniqueValuesList.add(valueBytesList);
            i+=n;
        }
        List<Byte> lastValueByteList = new ArrayList<>(sizeofLastKey);
        i = n * (uniqueValuesCount-1);
        while(i < uniqueValuesBytes.length){
            lastValueByteList.add(uniqueValuesBytes[i++]);
        }
        uniqueValuesList.add(lastValueByteList);
        return uniqueValuesList;
    }

    private byte[] getCodesLengths(InputStream inputStream, int uniqueValuesCount) throws IOException{
        return inputStream.readNBytes(uniqueValuesCount);
    }

    private List<StringBuilder> getCodes(
            InputStream inputStream,
            int uniqueValuesCount,
            byte[] codesLengths
    ) throws IOException{
        long totalNumberOfBits = 0;
        for(byte b: codesLengths){totalNumberOfBits += b;}

        int byteCount = (int)Math.ceilDiv(totalNumberOfBits, 8);
        byte[] buffer = new byte[byteCount];
        inputStream.readNBytes(buffer, 0, byteCount);

        List<StringBuilder> codesList = new ArrayList<>(uniqueValuesCount);
        BitStringReader reader = new BitStringReader(buffer);
        for(int l: codesLengths){
            StringBuilder stringBuilder = reader.getNextCode(l);
            codesList.add(stringBuilder);
        }
        return codesList;
    }

    private void decodeFile(
            InputStream inputStream,
            OutputStream outputStream,
            List<StringBuilder> codesList,
            List<List<Byte>> uniqueValuesList,
            long originalBytesCount
    )throws IOException{
        Map<String, List<Byte>> decoderMap = new HashMap<>();
        for(int i = 0; i < codesList.size(); i++){
            decoderMap.put(
                    codesList.get(i).toString(),
                    uniqueValuesList.get(i)
            );
        }

        long loadedBytes = 0;
        FileBufferedBitReader reader =
                new FileBufferedBitReader(BUFFER_SIZE, inputStream);
        while(loadedBytes < originalBytesCount){
            StringBuilder stringBuilder = new StringBuilder();
            List<Byte> uniqueValueList;
            while((uniqueValueList =
                    decoderMap.get(stringBuilder.toString())) == null){
                stringBuilder.append(reader.getNext());
            }
            byte[] uniqueValue = new byte[uniqueValueList.size()];
            for(int i = 0; i < uniqueValue.length; i++){
                uniqueValue[i] = uniqueValueList.get(i);
            }
            outputStream.write(uniqueValue, 0, uniqueValue.length);
            loadedBytes += uniqueValue.length;
        }
    }

    public void decompressFile() throws IOException {
        InputStream inputStream = new BufferedInputStream(
                new FileInputStream(filePath)
        );
        OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(getOutFilePath())
        );

        getN(inputStream);
        long originalFileSize = getOriginalFileSize(inputStream);
        int uniqueValuesCount = getUniqueValuesCount(inputStream);
        int sizeOfLastKey = getSizeOfLastKey(inputStream);
        List<List<Byte>> uniqueValuesList =
                getUniqueValues(inputStream, uniqueValuesCount, sizeOfLastKey);
        byte[] codesLengths = getCodesLengths(inputStream, uniqueValuesCount);
        List<StringBuilder> codesList =
                getCodes(inputStream, uniqueValuesCount, codesLengths);
        decodeFile(
                inputStream,
                outputStream,
                codesList,
                uniqueValuesList,
                originalFileSize
        );

        inputStream.close();
        outputStream.close();
    }

    public String getOutFilePath(){
        return this.outFilePath;
    }
}
