import java.io.File;
import java.util.HashSet;

public class Main {

    private static void checkExist(File file) {
        if (!file.exists()) {
            System.err.println(String.format("File '%s' does not exist!", file.toString()));
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Two args needed");
            System.exit(-1);
        }

        File src1 = new File(args[0]);
        File src2 = new File(args[1]);
        checkExist(src1);
        checkExist(src2);

        HashSet<String> changedMethods = MethodDiff.methodDiffInClass(src1.toString(), src2.toString());
        changedMethods.forEach(System.out::println);
    }
}
