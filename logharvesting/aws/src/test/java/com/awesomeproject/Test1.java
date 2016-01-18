package com.awesomeproject;

/**
 * @author chris_ge
 */
public class Test1 {

    Test2 t2;

    public Test1(Test2 t2) {
        this.t2 = t2;
    }

    public void init() {
        t2.init();

    }

    public void run() throws InterruptedException {

        for (int i = 0; i < 6; i++) {
            Thread.sleep(1L);
            System.out.println("i ===================> " + i);
            if (i == 3) t2.shutdown();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        Test2 t2 = new Test2();

        Test1 t1 = new Test1(t2);

        t1.init();
        t1.run();
    }

}
