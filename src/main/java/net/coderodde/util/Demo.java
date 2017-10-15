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
    
    private static int findLowerBound(Integer[] array, int fromIndex, int toIndex, Integer value) {
        int bound = 1;
        int rangeLength = toIndex - fromIndex;
        
        while (bound < rangeLength && array[bound].compareTo(value) < 0) {
            bound <<= 1;
        }
        
        return lowerBound(array, bound >>> 1, Math.min(toIndex, bound), value);
    }
        
    private static int findUpperBound(Integer[] array, int fromIndex, int toIndex, Integer value) {
        int bound = 1;
        int rangeLength = toIndex - fromIndex;
        
        while (bound < rangeLength && array[bound].compareTo(value) < 0) {
            bound <<= 1;
        }
        
        return upperBound(array, bound >>> 1, Math.min(toIndex, bound), value);
    }
    public static void main(String[] args) {
        Integer[] array = { 2, 4, 5, 1, 3, 4 };
        System.out.println(findLowerBound(array, 0, 3, 2));
    }
}
