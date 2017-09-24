package net.coderodde.util;

import java.util.Arrays;

public final class Demo {

    public static void main(String[] args) {
        Integer[] arr = {4, 5, 6, 3, 2, 1, 8, 3, 8 };
        AdaptiveMergesort.sort(arr, 1, arr.length);
        System.out.println(Arrays.toString(arr));
    }
}
