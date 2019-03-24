import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import result.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Iterator;

public class Main {
    private static String cve;
    private static String date;

    private static void checkExist(File file) {
        if (!file.exists()) {
            System.err.println(String.format("File '%s' does not exist!", file.toString()));
            System.exit(-1);
        }
    }

    private static void checkSetCveDate(File f1, File f2) {
        for (Path path : f1.toPath()) {
            String ps = path.toString();
            if (ps.contains("-")) {
                if (ps.startsWith("20")) {
                    date = ps;
                }
                if (ps.startsWith("CVE-")) {
                    cve = ps;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Three args needed: prettyprint=1|0, file1, file2");
            System.exit(-1);
        }

        int ppprint = Integer.parseInt(args[0]);
        if (ppprint != 1 && ppprint !=0) {
            throw new IllegalArgumentException("prettyprint 1 or 0");
        }

        File src1 = new File(args[1]);
        File src2 = new File(args[2]);
        checkExist(src1);
        checkExist(src2);
        checkSetCveDate(src1, src2);

        CmpJavaSrc cmpJavaSrc = new CmpJavaSrc(src1, src2);
        cmpJavaSrc.compare();

        Result result = cmpJavaSrc.getResult().prune();
        result.setCve(cve);
        result.setDate(date);

        Gson gson;
        if (ppprint == 1) gson = new GsonBuilder().setPrettyPrinting().create();
        else gson = new GsonBuilder().create();

        String json = gson.toJson(result);
        System.out.println(json);
    }
}
