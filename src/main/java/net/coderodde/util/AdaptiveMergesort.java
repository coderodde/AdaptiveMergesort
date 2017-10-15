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
                    // Bounce the lonely leftover run back to the tail of the 
                    // queue:
                    queue.enqueue(queue.dequeue());
                    // Fall through!
                    
                case 0:
                    runsLeft = queue.size();
                    continue;
            }
            
            Run run1 = queue.dequeue();
            Run run2 = queue.dequeue();
            queue.enqueue(merge(aux, run1, run2));
        }
        
        for (Interval interval = queue.dequeue().first; 
                interval != null; 
                interval = interval.next) {
            for (int i = interval.from; i <= interval.to; ++i) {
                array[fromIndex + i] = aux[i];
            }
        }
    }
    
    static <T extends Comparable<? super T>> Run merge(T[] aux,
                                                               Run run1, 
                                                               Run run2) {
        Interval headInterval1 = run1.first;
        Interval headInterval2 = run2.first;
        Interval mergedRunHead = null;
        Interval mergedRunTail = null;
        
        while (headInterval1 != null && headInterval2 != null) {
            T head1 = aux[headInterval1.from];
            T head2 = aux[headInterval2.from];
            
            if (head1.compareTo(head2) <= 0) {
                T splitValue = aux[headInterval2.from];
                int index = findLowerBound(aux,
                                           headInterval1.from,
                                           headInterval1.to + 1,
                                           splitValue);
                
                if (index == headInterval2.to) {
                    headInterval2 = headInterval2.next;
                }
                
                Interval newInterval = new Interval(headInterval1.from, index);
                headInterval1.from = index + 1;
                
                if (mergedRunHead == null) {
                    mergedRunHead = newInterval;
                    mergedRunTail = newInterval;
                } else {
                    mergedRunTail.next = newInterval;
                    newInterval.prev = mergedRunTail;
                    mergedRunTail = newInterval;
                }
            } else {
                T splitValue = aux[headInterval1.from];
                int index = findUpperBound(aux, 
                                           headInterval2.from,
                                           headInterval2.to + 1,
                                           splitValue);
                
                if (index == headInterval1.to) {
                    headInterval1 = headInterval1.next;
                }
                
                Interval newInterval = new Interval(headInterval2.from, index);
                headInterval2.from = index + 1;
                
                if (mergedRunHead == null) {
                    mergedRunHead = newInterval;
                    mergedRunTail = newInterval;
                } else {
                    mergedRunTail.next = newInterval;
                    newInterval.prev = mergedRunTail;
                    mergedRunTail = newInterval;
                }
            }
        }
        
        if (headInterval1 != null) {
            mergedRunTail.next = headInterval1;
            headInterval1.prev = mergedRunTail;
            mergedRunTail = headInterval1;
        } else {
            mergedRunTail.next = headInterval2;
            headInterval2.prev = mergedRunTail;
            mergedRunTail = headInterval2;
        }
        
        run1.first = mergedRunHead;
        run1.last = mergedRunTail;
        return run1;
    }
    
    private static <T extends Comparable<? super T>> 
        Run mergeOld(T[] aux, Run run1, Run run2) {
        Interval targetHead = null;
        Interval targetTail = null;
        Interval source1 = run1.first;
        Interval source2 = run2.first;
        boolean splitFirstRun;
        
        if (aux[run1.first.from].compareTo(aux[run2.first.from]) <= 0) {
            splitFirstRun = true;
        } else {
            splitFirstRun = false;
        }
        
        while (source1 != null && source2 != null) {
            if (splitFirstRun) {
                int index = findLowerBound(aux,
                                           source1.from,
                                           source1.to + 1,
                                           aux[source2.from]);
                
                Interval newInterval = new Interval(source1.from, index);
                source1.from = index;
                
                if (targetHead == null) {
                    targetHead = newInterval;
                    targetTail = newInterval;
                } else {
                    targetTail.next = newInterval;
                    newInterval.prev = targetTail;
                    targetTail = newInterval;
                }
            } else {
                int index = findLowerBound(aux,
                                           source2.from,
                                           source2.to + 1,
                                           aux[source1.from]);
                
                Interval newInterval = new Interval(source2.from, index);
                source2.from = index;
                
                if (targetHead == null) {
                    targetHead = newInterval;
                    targetTail = newInterval;
                } else {
                    targetTail.next = newInterval;
                    newInterval.prev = targetTail;
                    targetTail = newInterval;
                }
            }
            
            splitFirstRun = !splitFirstRun;
        }
        
        if (source1 != null) {
            targetTail.next = source1;
            source1.prev = targetTail;
            targetTail = source1;
        } else {
            targetTail.next = source2;
            source2.prev = targetTail;
            targetTail = source2;
        }
        
        run1.first = targetHead;
        run1.last = targetTail;
        return run1;
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
    
    private static <T extends Comparable<? super T>> 
        int lowerBound(T[] array, int fromIndex, int toIndex, T value) {
        int count = toIndex - fromIndex;
        int it;
        
        while (count > 0) {
            it = fromIndex;
            int step = count >>> 1;
            it += step;
            
            if (array[it].compareTo(value) < 0) {
                fromIndex = ++it;
                count -= step + 1;
            } else {
                count = step;
            }
        }
        
        return fromIndex;
    }
    
    private static <T extends Comparable<? super T>>
        int upperBound(T[] array, int fromIndex, int toIndex, T value) {
        int count = toIndex - fromIndex;
        int it;
        
        while (count > 0) {
            it = fromIndex; 
            int step = count >>> 1;
            it += step;
            
            if (array[it].compareTo(value) <= 0) {
                fromIndex = ++it;
                count -= step + 1;
            } else {
                count = step;
            }
        }
        
        return fromIndex;
    }
    
    private static <T extends Comparable<? super T>> 
        int findLowerBound(T[] array, int fromIndex, int toIndex, T value) {
        int bound = 1;
        int rangeLength = toIndex - fromIndex;
        
        while (bound < rangeLength && array[bound].compareTo(value) < 0) {
            bound <<= 1;
        }
        
        return lowerBound(array, bound >>> 1, Math.min(toIndex, bound), value);
    }
        
    private static <T extends Comparable<? super T>> 
        int findUpperBound(T[] array, int fromIndex, int toIndex, T value) {
        int bound = 1;
        int rangeLength = toIndex - fromIndex;
        
        while (bound < rangeLength && array[bound].compareTo(value) < 0) {
            bound <<= 1;
        }
        
        return upperBound(array, bound >>> 1, Math.min(toIndex, bound), value);
    }
}
