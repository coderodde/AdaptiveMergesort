package net.coderodde.util;

import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public final class AdaptiveMergesortTest {

    private static final int BRUTE_FORCE_ITERATIONS = 1000;
    private static final int MAXIMUM_ARRAY_LENGTH = 100;
    private static final int MIN_ELEMENT = -50;
    private static final int MAX_ELEMENT = 50;
    private static final int MINIMUM_ARRAY_LENGTH = 3;

    @Test
    public void testBruteForce() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);

        System.out.println("AdaptiveMergesortTest.testBruteForce(): seed = " +
                seed);

        for (int i = 0; i < BRUTE_FORCE_ITERATIONS; ++i) {
            int arrayLength = random.nextInt(MAXIMUM_ARRAY_LENGTH + 1);
            arrayLength = Math.max(arrayLength, MINIMUM_ARRAY_LENGTH);

            int fromIndex = random.nextInt(arrayLength / 2);
            int toIndex = arrayLength - random.nextInt(arrayLength / 2);
            Integer[] array1 = getRandomArray(arrayLength, random);
            Integer[] array2 = array1.clone();

            Arrays.sort(array1, fromIndex, toIndex);
            AdaptiveMergesort.sort(array2, fromIndex, toIndex);

            assertTrue(Arrays.equals(array1, array2));
        }
    }

    @Test(expected = NullPointerException.class)
    public void throwsOnNullArray() {
        AdaptiveMergesort.sort(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsOnReversedFromIndexToIndex() {
        AdaptiveMergesort.sort(new Integer[]{ 1, 2, 3 }, 1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testThrowsOnNegativeFromIndex() {
        AdaptiveMergesort.sort(new Integer[]{ 1, 2, 3}, -1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testThrowsOnTooLargeToIndex() {
        AdaptiveMergesort.sort(new Integer[]{ 1, 2, 3 }, 1, 4);
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

    private static Integer[] getRandomArray(int length, Random random) {
        Integer[] array = new Integer[length];

        for (int i = 0; i < length; ++i) {
            array[i] = MIN_ELEMENT + 
                       random.nextInt(MAX_ELEMENT - MIN_ELEMENT + 1);
        }

        return array;
    }
}
