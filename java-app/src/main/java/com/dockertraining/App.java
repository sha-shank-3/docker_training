package com.dockertraining;

public class App {

    public static void main(String[] args) {
        System.out.println("Hello from Docker Training Java App!");
        MathUtils math = new MathUtils();
        System.out.println("5 + 3 = " + math.add(5, 3));
        System.out.println("10 - 4 = " + math.subtract(10, 4));
        System.out.println("6 * 7 = " + math.multiply(6, 7));
    }
}
