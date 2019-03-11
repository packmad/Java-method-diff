import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class CmpJavaSrcTest {

    private ClassLoader classLoader;

    @Before
    public void initClassloader() {
        classLoader = this.getClass().getClassLoader();
    }

    @Test
    public void compare() throws IOException {
        URL url_src1 = classLoader.getResource("test.txt");
        InputStream is = url_src1.openStream();
    }
}