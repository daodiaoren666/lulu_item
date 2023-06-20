package com.lulu.usercenter;

import com.lulu.usercenter.ocne.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class algorithmTest {
    @Test
    public void test(){
        List<String> list = Arrays.asList("java", "大一", "男");
        List<String> list1 = Arrays.asList("java", "大二", "男");
        List<String> list2 = Arrays.asList("c++", "大二", "女");
        int i1 = AlgorithmUtils.minDistance(list, list1);
        int i2 = AlgorithmUtils.minDistance(list, list2);
        System.out.println(i1);
        System.out.println(i2);
    }
}
