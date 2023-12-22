import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        int x = Integer.MAX_VALUE;
        byte[] b = Main.serialize(x);
        System.out.println(Main.deserializeInt(Main.serialize(x)));
        System.out.println(Arrays.toString(Main.serialize(x)));
    }
}
