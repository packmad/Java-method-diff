import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;

public class Main {

    private static void checkExist(File file) {
        if (!file.exists()) {
            System.err.println(String.format("File '%s' does not exist!", file.toString()));
            System.exit(-1);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

        if (args.length != 2) {
            System.err.println("Two args needed");
            System.exit(-1);
        }

        File src1 = new File(args[0]);
        File src2 = new File(args[1]);
        checkExist(src1);
        checkExist(src2);

        CmpJavaSrc cmpJavaSrc = new CmpJavaSrc(src1, src2);
        cmpJavaSrc.compare();

        Result result = cmpJavaSrc.getResult();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(result);
        System.out.println(json);
    }
}
