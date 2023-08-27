package com.example.test.util;

import java.util.Random;

public class TestUtils {

    public static Long getRandomCustomerId() {
        return new Random().nextLong(1000000000L, 9999999999L);
    }
}
