package net.coderodde.util;

import java.util.Arrays;

public final class Demo {

    public static int lowerBound(Integer[] array, 
                                 int fromIndex, 
                                 int toIndex, 
                                 Integer value) {
        int count = toIndex - fromIndex;
        int it;
        
        while (count > 0) {
            it = fromIndex;
            int step = count >>> 1;
            it += step;
            
            if (array[it] < value) {
                fromIndex = ++it;
                count -= step + 1;
            } else {
                count = step;
            }
        }
        
        return fromIndex;
    }
    
    public static int upperBound(Integer[] array,
                                 int fromIndex, 
                                 int toIndex,
                                 Integer value) {
        int count = toIndex - fromIndex;
        int it;
        
        while (count > 0) {
            it = fromIndex; 
            int step = count >>> 1;
            it += step;
            
            if (value >= array[it]) {
                fromIndex = ++it;
                count -= step + 1;
            } else {
                count = step;
            }
        }
        
        return fromIndex;
    }
    
    public static void main(String[] args) {
        Integer[] arr = { 1, 2, 4, 7, 10, 10, 11, 14, 14, 19, 20 };
        System.out.println("Lower bound: " + lowerBound(arr, 1, arr.length - 1, 10));
        System.out.println("Upper bound: " + upperBound(arr, 1, arr.length - 1, 10));
    }
}
