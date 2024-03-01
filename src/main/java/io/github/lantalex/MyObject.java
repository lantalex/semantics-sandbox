package io.github.lantalex;

public class MyObject {

    public int a;
    public int b;
    public int c;
    public int d;

    public MyObject(int value) {
        a = value;
        b = value;
        c = value;
        d = value;
    }

    public int getSum() {
        return a + b + c + d;
    }
}
