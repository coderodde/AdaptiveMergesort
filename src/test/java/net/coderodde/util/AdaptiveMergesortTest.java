package net.coderodde.util;

import java.util.Arrays;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public final class AdaptiveMergesortTest {
    
    private static final int BRUTE_FORCE_ITERATIONS = 10;
    private static final int MAXIMUM_ARRAY_LENGTH = 10;
   
    
    @Test
    public void testBruteForce() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        
        System.out.println("AdaptiveMergesortTest.testBruteForce(): seed = " +
                seed);
        
        for (int i = 0; i < BRUTE_FORCE_ITERATIONS; ++i) {
            int arrayLength = random.nextInt(MAXIMUM_ARRAY_LENGTH + 1);
            
            
        }
    }
    
    @Test(expected = NullPointerException.class)
    public void throwsOnNullArray() {
        AdaptiveMergesort.sort(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void throwsOnReversedFromIndexToIndex() {
        AdaptiveMergesort.sort(new Integer[]{}, 1, 0);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void throwsOnNegativeFromIndex() {
        AdaptiveMergesort
    }
   
    @Test
    public void testEmptyArrayWithIndices() {
        Integer[] array = { 4, 1, 8, 2 };
        
        for (int i = 0; i < array.length; ++i) {
            AdaptiveMergesort.sort(array, i, i);
        }
            
        assertTrue(Arrays.equals(array, new Integer[]{ 4, 1, 8, 2}));
    }
    
    @Test
    public void testOneElementArrayWithIndices() {
        Integer[] array = { 8, 3, 6, 1 };
        
        for (int i = 0; i < array.length - 1; ++i) {
            AdaptiveMergesort.sort(array, i, i + 1);
        }
        
        assertTrue(Arrays.equals(array, new Integer[]{ 8, 3, 6, 1}));
    }
    
    @Test
    public void testEmptyArray() {
        Integer[] array = {};
        AdaptiveMergesort.sort(array);
    }
    
    @Test
    public void testOneElementArray() {
        Integer[] array = { 0 };
        AdaptiveMergesort.sort(array);
        assertTrue(Arrays.equals(array, new Integer[]{ 0 }));
    }
}
