package net.coderodde.util;

import java.util.Arrays;
import java.util.Random;

public final class Demo {

    private static final int FROM_INDEX = 7;
    private static final int SKIP_RIGHT = 9;
    private static final int ARRAY_LENGTH = 1000;
    private static final int BLOCKS = 30;
    private static final int MIN_ELEMENT = -1000;
    private static final int MAX_ELEMENT = 1000;
    
    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        
        warmup(random);
        benchmark(random);
    }
    
    private static void warmup(Random random) {
        System.out.println("Warming up...");
        
        Integer[] array = getBlockedArray(ARRAY_LENGTH, BLOCKS, random);
        warmup(array);
        
        array = getRandomArray(ARRAY_LENGTH, random);
        warmup(array);
        
        System.out.println("Warming up done!\n\n");
    }
    
    private static void benchmark(Random random) {
        Integer[] array = getBlockedArray(ARRAY_LENGTH, BLOCKS, random);
        warmup(array);
        
        array = getRandomArray(ARRAY_LENGTH, random);
        warmup(array);
    }
    
    private static void warmup(Integer[] array1) {
        perform(false, array1);
    }
    
    private static void benchmark(Integer[] array1) {
        perform(true, array1);
    }
    
    private static void perform(boolean output, 
                                Integer[] array1) {
        Integer[] array2 = array1.clone();
        int length = array1.length;
        
        long startTime = System.currentTimeMillis();
        Arrays.sort(array1, FROM_INDEX, length - SKIP_RIGHT);
        long endTime = System.currentTimeMillis();
        
        if (output) {
            System.out.println("Arrays.sort in " + (endTime - startTime) + 
                               " milliseconds.");
        }
        
        startTime = System.currentTimeMillis();
        AdaptiveMergesort.sort(array2, FROM_INDEX, length - SKIP_RIGHT);
        endTime = System.currentTimeMillis();
        
        if (output) {
            System.out.println("AdaptiveMergesort.sort in " + 
                               (endTime - startTime) +
                               " milliseconds.");
        }
    }
    
    private static final Integer[] getBlockedArray(int length, 
                                                   int blocks,
                                                   Random random) {
        Integer[] array = getAscendingArray(length);
        blockify(array, blocks, random);
        return array;
    }
    
    private static final Integer[] getRandomArray(int length, Random random) {
        Integer[] array = new Integer[length];
        
        for (int i = 0; i < length; ++i) {
            array[i] = random.nextInt(MAX_ELEMENT - MIN_ELEMENT + 1) + MIN_ELEMENT;
        }
        
        return array;
    }
    
    private static final Integer[] getAscendingArray(int length) {
        Integer[] array = new Integer[length];
        
        for (int i = 0; i < length; ++i) {
            array[i] = i;
        }
        
        return array;
    }
    
    private static void blockify(Integer[] array,
                                 int numberOfBlocks, 
                                 Random random) {
        int blockSize = array.length / numberOfBlocks;
        Integer[][] blocks = new Integer[numberOfBlocks][];
        
        for (int i = 0; i < numberOfBlocks - 1; ++i) {
            blocks[i] = new Integer[blockSize];
        }
        
        blocks[numberOfBlocks - 1] = 
                new Integer[blockSize + array.length % blockSize];
        
        int index = 0;
        
        for (Integer[] block : blocks) {
            for (int i = 0; i < block.length; ++i) {
                block[i] = array[index++];
            }
        }
        
        shuffle(blocks, random);
        
        index = 0;
        
        for (Integer[] block : blocks) {
            for (int i = 0; i < block.length; ++i) {
                array[index++] = block[i];
            }
        }
    }
    
    private static void shuffle(Integer[][] blocks, Random random) {
        for (int i = 0; i < blocks.length; ++i) {
            int index1 = random.nextInt(blocks.length);
            int index2 = random.nextInt(blocks.length);
            Integer[] block = blocks[index1];
            blocks[index1] = blocks[index2];
            blocks[index2] = block;
        }
    }
}
