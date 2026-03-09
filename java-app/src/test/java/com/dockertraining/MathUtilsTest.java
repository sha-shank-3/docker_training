package com.dockertraining;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MathUtilsTest {

    private final MathUtils math = new MathUtils();

    @Test
    void testAdd() {
        assertEquals(8, math.add(5, 3));
        assertEquals(0, math.add(-1, 1));
        assertEquals(-5, math.add(-3, -2));
    }

    @Test
    void testSubtract() {
        assertEquals(6, math.subtract(10, 4));
        assertEquals(-2, math.subtract(3, 5));
    }

    @Test
    void testMultiply() {
        assertEquals(42, math.multiply(6, 7));
        assertEquals(0, math.multiply(0, 100));
        assertEquals(-12, math.multiply(-3, 4));
    }

    @Test
    void testDivide() {
        assertEquals(2.5, math.divide(5, 2));
        assertEquals(-3.0, math.divide(-6, 2));
    }

    @Test
    void testDivideByZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> math.divide(10, 0));
    }
}
