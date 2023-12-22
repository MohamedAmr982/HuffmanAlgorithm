import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HuffmanUtility {

    static byte[] serializeLong(long val){
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
        arr[0] = (byte)((val >> 56) & bitmask);
        return arr;
    }

    static byte[] serializeInt(int val){
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

    static List<Map.Entry<List<Byte>, StringBuilder>> getCodesList(int n, Map<List<Byte>, StringBuilder> codesMap){
        List<Map.Entry<List<Byte>, StringBuilder>> entryList = new ArrayList<>();
        Map.Entry<List<Byte>, StringBuilder> remainderEntry = null;
        for(var entry: codesMap.entrySet()){
            if(entry.getKey().size() < n){
                remainderEntry = entry;
            }else{
                entryList.add(entry);
            }
        }
        if(remainderEntry != null){
            entryList.add(remainderEntry);
        }
        return entryList;
    }

    static byte[][] serializeKeys(int keySize, List<Map.Entry<List<Byte>, StringBuilder>> codesList){
        byte[][] keyBytes = new byte[codesList.size()][keySize];
        for(int i = 0; i < codesList.size()-1; i++){
            for(int j = 0; j < keySize; j++){
                keyBytes[i][j] = codesList.get(i).getKey().get(j);
            }
        }
        //handling remainderEntry
        var remainderEntry
                = codesList.get(codesList.size()-1);
        int i;
        for(i = 0; i < remainderEntry.getKey().size(); i++){
            keyBytes[keyBytes.length-1][i] = remainderEntry.getKey().get(i);
        }
        while(i < keySize){
            //padding
            keyBytes[keyBytes.length-1][i++] = (byte)0;
        }
        return keyBytes;
    }

    static byte[] serializeCodeLengths(List<Map.Entry<List<Byte>, StringBuilder>> codesList){
        byte[] arr = new byte[codesList.size()];
        int i = 0;
        for(var entry: codesList){
            arr[i++] = (byte)entry.getValue().length();
        }
        return arr;
    }

    static byte[] serializeCodes2(Map<List<Byte>, StringBuilder> codesMap){
        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte)0);
        int indexInByte = 7;
        Byte currByte = byteList.get(0);
        for(StringBuilder stringBuilder: codesMap.values()){
            int indexInStringBuilder = 0;
            while(indexInStringBuilder < stringBuilder.length()){
                if(indexInByte < 0){
                    byteList.set(byteList.size()-1, currByte);
                    byteList.add((byte)0);
                    currByte = byteList.get(byteList.size()-1);
                    indexInByte = 7;
                }
                currByte = (byte) (currByte |
                        ((Integer.parseInt(""+stringBuilder.charAt(indexInStringBuilder))) << indexInByte));
                indexInByte--; indexInStringBuilder++;
            }
        }
        if(indexInByte < 7){byteList.set(byteList.size()-1, currByte);}
        byte[] byteArr = new byte[byteList.size()];
        indexInByte = 0;
        for(Byte b: byteList){byteArr[indexInByte++] = b;}
        return byteArr;
    }

    static byte[] serializeCodes(List<Map.Entry<List<Byte>, StringBuilder>> codesList){
        ArrayBitWriter writer = new ArrayBitWriter();
        for(var entry: codesList){
            StringBuilder stringBuilder = entry.getValue();
            for(int i = 0; i < stringBuilder.length(); i++){
                writer.writeBit(
                        Integer.parseInt(""+stringBuilder.charAt(i))
                );
            }
        }
        return writer.get();
    }



}
