import java.io.File;

public class Main {

    public static void main(String[] args) {
        if(args[0] == null){
            System.out.println("Please provide (c/d) to choose from compression/decompression, " +
                    "absolute path to the file, and n (in case of compression).");
            System.exit(1);
        }
        try{
            long time = System.currentTimeMillis();

            if(HuffmanUtility.getFileSize(args[1]) == 0){
                System.out.println("Empty file!!!");
                System.exit(1);
            }

            if(args[0].equals("c")){
                HuffmanEncoder encoder =
                        new HuffmanEncoder(Integer.parseInt(args[2]), args[1]);
                encoder.compressFile();
                System.out.printf("Compression ratio = %2f%%\n", encoder.getCompressionRatio()*100);
            }else if(args[0].equals("d")){
                new HuffmanDecoder(args[1]).decompressFile();
            }

            time  = System.currentTimeMillis() - time;
            System.out.println("Took "+
                    (time/1000)/60 + " min "
                    + (time/1000)%60 + " sec "
                    + time%1000 +" ms");
        }catch (Exception e){
            System.out.println("Error!!!");
            e.printStackTrace();
        }
    }
}