package net.coderodde.util;

import java.util.Arrays;
import java.util.Objects;

public final class AdaptiveMergesort {

    private AdaptiveMergesort() {}
    
    public static <T extends Comparable<? super T>> void sort(T[] array) {
        Objects.requireNonNull(array, "The input array is null.");
        sort(array, 0, array.length);
    }
    
    public static <T extends Comparable<? super T>> void sort(T[] array,
                                                              int fromIndex,
                                                              int toIndex) {
        Objects.requireNonNull(array, "The input array is null.");
        checkIndices(array.length, fromIndex, toIndex);
        
        int rangeLength = toIndex - fromIndex;
        
        if (rangeLength < 2) {
            return; // Trivially sorted.
        }
        
        T[] aux = Arrays.copyOfRange(array, fromIndex, toIndex);
        RunQueue queue = new RunLengthQueueBuilder<>(aux).run();
        int runsLeft = queue.size();
        
        while (queue.size() > 0) {
            switch (runsLeft) {
                case 1:
                    queue.enqueue(queue.dequeue());
                    
                case 0:
                    runsLeft = queue.size();
                    continue;
            }
            
            Run run1 = queue.dequeue();
            Run run2 = queue.dequeue();
            queue.enqueue(merge(aux, run1, run2));
        }
    }
    
    private static <T extends Comparable<? super T>> 
        Run merge(T[] aux, Run run1, Run run2) {
            
        return null;
    }
    
    private static void checkIndices(int arrayLength, 
                                     int fromIndex, 
                                     int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException(
                    "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(
                    "fromIndex = " + fromIndex);
        }
        
        if (toIndex > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(
                    "toIndex = " + toIndex);
        }
    }
    
    private static final class Interval {
        int from;
        int to;
        Interval prev;
        Interval next;
        
        Interval(int from, int to) {
            this.from = from;
            this.to = to;
        }
        
        @Override
        public String toString() {
            return "(" + from + ", " + to + ")";
        }
    }
    
    private static final class Run {
        Interval first;
        Interval last;
        
        Run(int from, int to) {
            first = new Interval(from, to);
            last = first;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            String separator = "";
            
            for (Interval interval = first; 
                    interval != null; 
                    interval = interval.next) {
                sb.append(separator).append(interval);
                separator = ", ";
            }
            
            return sb.append("]").toString();
        }
    }
    
    private static final class RunQueue {
        
        private final Run[] runArray;
        private final int mask;
        private int head;
        private int tail;
        private int size;
        
        RunQueue(int capacity) {
            capacity = ceilCapacityToPowerOfTwo(capacity);
            this.mask = capacity - 1;
            this.runArray = new Run[capacity];
        }
        
        void enqueue(Run run) {
            runArray[tail] = run;
            tail = (tail + 1) & mask;
            ++size;
        }
        
        void addToLastRun(int runLength) {
            runArray[(tail - 1) & mask].first.to += runLength;
        }
        
        Run dequeue() {
            Run run = runArray[head];
            head = (head + 1) & mask;
            --size;
            return run;
        }
        
        int size() {
            return size;
        }
        
        private static int ceilCapacityToPowerOfTwo(int capacity) {
            int ret = Integer.highestOneBit(capacity);
            return ret != capacity ? ret << 1 : ret;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            String separator = "";
            
            for (int i = 0; i < size; ++i) {
                sb.append(separator).append(runArray[(head + i) & mask]);
                separator = ", ";
            }
            
            return sb.append("]").toString();
        }
    }
    
    private static final class 
            RunLengthQueueBuilder<T extends Comparable<? super T>> {
        
        private final RunQueue queue;
        private final T[] array;
        private int head;
        private int left;
        private int right;
        private final int last;
        private boolean previousRunWasDesending;
        
        RunLengthQueueBuilder(T[] array) {
            this.queue = new RunQueue((array.length >>> 1) + 1);
            this.array = array;
            this.left  = 0;
            this.right = 1;
            this.last  = array.length - 1;
        }
        
        RunQueue run() {
            while (left < last) {
                head = left;
                
                if (array[left++].compareTo(array[right++]) <= 0) {
                    scanAscendingRun();
                } else {
                    scanDescendingRun();
                }
                
                ++left;
                ++right;
            }
            
            if (left == last) {
                if (array[last - 1].compareTo(array[last]) <= 0) {
                    queue.addToLastRun(1);
                } else {
                    queue.enqueue(new Run(left, left));
                }
            }
            
            return queue;
        }
        
        void scanAscendingRun() {
            while (left < last && array[left].compareTo(array[right]) <= 0) {
                ++left;
                ++right;
            }
            
            Run run = new Run(head, left);
            
            if (previousRunWasDesending) {
                if (array[head - 1].compareTo(array[head]) <= 0) {
                    queue.addToLastRun(right - head);
                } else {
                    queue.enqueue(run);
                }
            } else {
                queue.enqueue(run);
            }
            
            previousRunWasDesending = false;
        }
        
        void scanDescendingRun() {
            while (left < last && array[left].compareTo(array[right]) > 0) {
                ++left;
                ++right;
            }
            
            Run run = new Run(head, left);
            reverseRun(array, head, left);
            
            if (previousRunWasDesending) {
                if (array[head - 1].compareTo(array[head]) <= 0) {
                    queue.addToLastRun(right - head);
                } else {
                    queue.enqueue(run);
                }
            } else {
                queue.enqueue(run);
            }
            
            previousRunWasDesending = true;
        }
        
        private void reverseRun(T[] array, int i, int j) {
            for (; i < j; ++i, --j) {
                T tmp = array[i];
                array[i] = array[j];
                array[j] = tmp;
            }
        }
    }
}
