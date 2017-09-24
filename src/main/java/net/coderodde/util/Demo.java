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
            
            if (!(value < array[it])) {
                fromIndex += ++it;
                count -= step + 1;
            } else {
                count = step;
            }
        }
        
        return fromIndex;
    }
    
    public static void main(String[] args) {
        Integer[] arr = {4, 5, 6, 3, 2, 1, 8, 3, 8 };
        AdaptiveMergesort.sort(arr, 1, arr.length);
        System.out.println(Arrays.toString(arr));
    }
}
