package net.coderodde.util;

import java.util.Arrays;
import java.util.Random;

public final class Demo {

    private static final int FROM_INDEX = 7;
    private static final int SKIP_RIGHT = 9;
    private static final int ARRAY_LENGTH = 50_000;
    private static final int BLOCKS = 1000;
    private static final int MIN_ELEMENT = -10_000;
    private static final int MAX_ELEMENT = 10_000;
    private static final int MAX_RUN_LENGTH = 100;
    private static final int RUNS = 1000;

    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        System.out.println("Seed = " + seed);

        warmup(random);
        benchmark(random);
    }

    private static void warmup(Random random) {
        System.out.println("Warming up...");

        Integer[] array = getBlockedArray(ARRAY_LENGTH, BLOCKS, random);
        warmup(array);

        array = getRandomArray(ARRAY_LENGTH, random);
        warmup(array);
        
        array = getFunnyArray(ARRAY_LENGTH, random);
        warmup(array);
        
        array = getRunnyArray(ARRAY_LENGTH, RUNS, random);
        warmup(array);

        array = getZigZagArray(ARRAY_LENGTH);
        warmup(array);
        
        System.out.println("Warming up done!\n\n");
    }

    private static void benchmark(Random random) {
        Integer[] array = getBlockedArray(ARRAY_LENGTH, BLOCKS, random);
        System.out.println("--- Blocked array ---");
        benchmark(array);

        array = getRandomArray(ARRAY_LENGTH, random);
        System.out.println("--- Random array ----");
        benchmark(array);
        
        array = getFunnyArray(ARRAY_LENGTH, random);
        System.out.println("--- Funny array -----");
        benchmark(array);
        
        array = getRunnyArray(ARRAY_LENGTH, RUNS, random);
        System.out.println("--- Runny array -----");
        benchmark(array);
        
        array = getZigZagArray(ARRAY_LENGTH);
        System.out.println("--- Zig zag array ---");
        benchmark(array);
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

            System.out.println("Algorithms agree: " +
                               Arrays.equals(array1, array2));
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

    private static final Integer[] getFunnyArray(int length, Random random) {
        Integer[] array = new Integer[length];
        
        int index = 0;
        
        while (index < array.length) {
            int remaining = array.length - index;
            int next = random.nextInt(MAX_RUN_LENGTH);
            int actual = Math.min(remaining, next);
            boolean direction = random.nextBoolean();
            
            Integer first = 
                    MIN_ELEMENT + 
                    random.nextInt(MAX_ELEMENT - MIN_ELEMENT + 1);
            
            array[index++] = first;
            int step = 1 + random.nextInt(5);
            
            if (direction) {
                for (int i = 1; i < actual; ++i) {
                    array[index++] = first + i * step;
                }
            } else {
                for (int i = 1; i < actual; ++i) {
                    array[index++] = first - i * step;
                }
            }
        }
        
        return array;
    }
    
    private static final Integer[] getRunnyArray(int length,
                                                 int runLength, 
                                                 Random random) {
        Integer[] array = getRandomArray(length, random);
        
        int index = 0;
        
        while (index < length) {
            int remaining = length - index;
            int requested = random.nextInt(runLength);
            int actual = Math.min(remaining, requested);
            
            Arrays.sort(array, index, index + actual);
            index += actual;
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
    
    private static final Integer[] getZigZagArray(int length) {
        Integer[] array = getAscendingArray(length);
        
        for (int i = 0; i + 1 < length; i += 2) {
            Integer tmp = array[i];
            array[i] = array[i + 1];
            array[i + 1] = tmp;
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
