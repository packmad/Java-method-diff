/*
/usr/lib/jvm/java-8-oracle/bin/javac Foo.java && jar cvf foo.jar *.class && dx --dex --output=foo.dex foo.jar
 */

public abstract class Foo {
    private int i1;
    private static final int i2 = 0;

    private class Bar{
        private class Miao {
            private class Squack{
                int s;
                Squack() {
                    s = 42;
                }
        }}
    }

    public Foo(int i) {
        i1 = i
    }

    static void m1(){ }
}
