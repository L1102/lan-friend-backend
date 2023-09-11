package com.lan.utils;

import org.junit.jupiter.api.Test;

/**
 * @author lan
 */
class AlgorithmUtilsTest {

    @Test
    void testMinDistance1() {
        String str1="java";
        String str2="javva";
        String str3="javvava";
        int i = AlgorithmUtils.minDistance(str1, str2);
        System.out.println(i);
    }

    @Test
    void testMinDistance2() {
    }
}