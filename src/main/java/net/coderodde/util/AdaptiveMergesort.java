package net.coderodde.util;

import java.util.Arrays;
import java.util.Objects;

public final class AdaptiveMergesort {

    private AdaptiveMergesort() {}

    /**
     * Sorts stably the entire input array.
     * 
     * @param <T>   the array component type.
     * @param array the array to sort.
     */
    public static <T extends Comparable<? super T>> void sort(T[] array) {
        Objects.requireNonNull(array, "The input array is null.");
        sort(array, 0, array.length);
    }

    /**
     * Sorts stably the input subarray {@code array[fromIndex], 
     * array[fromIndex + 1], ..., array[toIndex - 2], array[toIndex - 1]}.
     * 
     * @param <T>       the array component type.
     * @param array     the array holding the target subarray.
     * @param fromIndex the index of the leftmost array component belonging to 
     *                  the requested array range.
     * @param toIndex   the index of the largest array component in the range   
     *                  plus one.
     */
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

        // Number of runs not yet processed in the current merge pass over the
        // data:
        int runsLeft = queue.size();

        while (queue.size() > 1) {
            switch (runsLeft) {
                case 1:
                    // Bounce the lonely leftover run back to the tail of the 
                    // queue:
                    queue.enqueue(queue.dequeue());
                    // Fall through!

                case 0:
                    // Get to know how many runs there is to process in the 
                    // next merge pass:
                    runsLeft = queue.size();
                    continue;
            }

            // Remove the first two consecutive runs, merge them and append the
            // resulting merged run to the tail of the run queue:
            queue.enqueue(merge(aux, queue.dequeue(), queue.dequeue()));
            // Update the number of runs not yet processed in this merge pass:
            runsLeft -= 2;
        }

        // Put the elements in their correct positions such that the input array
        // range becomes stabily sorted:
        int arrayIndex = fromIndex;

        for (Interval interval = queue.dequeue().first; 
                interval != null; 
                interval = interval.next) {
            for (int i = interval.from; i <= interval.to; ++i) {
                array[arrayIndex++] = aux[i];
            }
        }
    }

    private static <T extends Comparable<? super T>> Run merge(T[] aux,
                                                               Run run1, 
                                                               Run run2) {
        Interval headInterval1 = run1.first;
        Interval headInterval2 = run2.first;
        Interval mergedRunHead = null;
        Interval mergedRunTail = null;

        // While both the left and right runs have intervals to offer, do:
        while (headInterval1 != null && headInterval2 != null) {
            T head1 = aux[headInterval1.from];
            T head2 = aux[headInterval2.from];

            if (head1.compareTo(head2) <= 0) {
                T tail1 = aux[headInterval1.to];

                if (tail1.compareTo(head2) <= 0) {
                    // Easy case, just append one interval to the other:
                    if (mergedRunHead == null) {
                        mergedRunHead = headInterval1;
                        mergedRunTail = headInterval1;
                    } else {
                        mergedRunTail.next = headInterval1;
                        headInterval1.prev = mergedRunTail;
                        mergedRunTail = headInterval1;
                    }

                    headInterval1 = headInterval1.next;
                    continue;
                }

                // Cannot append. We need to split the left interval:
                int index = findUpperBound(aux,
                                           headInterval1.from,
                                           headInterval1.to + 1,
                                           head2);

                Interval newInterval = new Interval(headInterval1.from,
                                                    index - 1);

                // Remove some head elements from first interval:
                headInterval1.from = index;

                // Append a split interval to the tail of the merged run:
                if (mergedRunHead == null) {
                    mergedRunHead = newInterval;
                    mergedRunTail = newInterval;
                } else {
                    mergedRunTail.next = newInterval;
                    newInterval.prev = mergedRunTail;
                    mergedRunTail = newInterval;
                }
            } else {
                T tail2 = aux[headInterval2.to];

                if (tail2.compareTo(head1) < 0) {
                    // Easy case, just append one interval to the other:
                    if (mergedRunHead == null) {
                        mergedRunHead = headInterval2;
                        mergedRunTail = headInterval2;
                    } else {
                        mergedRunTail.next = headInterval2;
                        headInterval2.prev = mergedRunTail;
                        mergedRunTail = headInterval2;
                    }

                    headInterval2 = headInterval2.next;
                    continue;
                }

                // Cannot append. We need to split the right interval:
                int index = findLowerBound(aux, 
                                           headInterval2.from,
                                           headInterval2.to + 1,
                                           head1);

                Interval newInterval = new Interval(headInterval2.from,
                                                    index - 1);

                // Remove some head elements from second interval:
                headInterval2.from = index; 

                // Append a split interval to the tail of the merge run:
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

        // Append the leftover intervals of a currently non-empty run to the
        // tail of the merged run:
        mergedRunTail.next = headInterval1 != null ? headInterval1 :
                                                     headInterval2;
        mergedRunTail.next.prev = mergedRunTail;
        mergedRunTail = mergedRunTail.next;

        // Reuse 'run1' in order not to abuse the heap memory too often:
        run1.first = mergedRunHead;
        run1.last = mergedRunTail;
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

    /**
     * This class represents a sorted ascending interval. In other words,
     * {@code aux[from], ..., aux[to]} is a sorted ascending sequence (block).
     */
    private static final class Interval {
        int from;
        int to;
        Interval prev;
        Interval next;

        Interval(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    /**
     * Run represents a doubly-linked list of intervals such that the list
     * represents a sorted run.
     */
    private static final class Run {
        Interval first;
        Interval last;

        Run(int from, int to) {
            first = new Interval(from, to);
            last = first;
        }
    }

    /**
     * This class holds a queue of runs yet to merge.
     */
    private static final class RunQueue {

        private final Run[] runArray;
        // Used for bit level modulo arithmetic. Instead of
        // 'index % runArray.length' we can write 'index & mask'.
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

        /**
         * Extends the length of the tail run by {@code runLength} elements.
         * 
         * @param runLength the number of elements to add to the tail run.
         */
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

        /**
         * If {@code capacity} is not a power of two, this method ceils it up 
         * towards the smallest power of two no less than {@code capacity}.
         * 
         * @param capacity the candidate capacity.
         * @return a smallest power of two no less than {@code capacity}.
         */
        private static int ceilCapacityToPowerOfTwo(int capacity) {
            int ret = Integer.highestOneBit(capacity);
            return ret != capacity ? ret << 1 : ret;
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
                    // The next run is ascending:
                    scanAscendingRun();
                } else {
                    // The next run is descending:
                    scanDescendingRun();
                }

                ++left;
                ++right;
            }

            if (left == last) {
                // Deal with a single element run at the very tail of the input
                // array range:
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


            if (previousRunWasDesending) {
                if (array[head - 1].compareTo(array[head]) <= 0) {
                    // We can just extend the previous run:
                    queue.addToLastRun(right - head);
                } else {
                    queue.enqueue(new Run(head, left));
                }
            } else {
                queue.enqueue(new Run(head, left));
            }

            previousRunWasDesending = false;
        }

        void scanDescendingRun() {
            while (left < last && array[left].compareTo(array[right]) > 0) {
                ++left;
                ++right;
            }

            reverseRun(array, head, left);

            if (previousRunWasDesending) {
                if (array[head - 1].compareTo(array[head]) <= 0) {
                    // We can just extend the previous run:
                    queue.addToLastRun(right - head);
                } else {
                    queue.enqueue(new Run(head, left));
                }
            } else {
                queue.enqueue(new Run(head, left));
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

    /**
     * Returns the smallest index of an array component that does not compare 
     * less than {@code value}. 
     * 
     * @param <T>       the array component type.
     * @param array     the array holding the target range.
     * @param fromIndex the lowest index of the array range to process.
     * @param toIndex   the largest index of the array range to process plus
     *                  one.
     * @param value     the target value.
     * @return          the array index.
     */
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

    /**
     * Returns the smallest index of an array component that compares greater 
     * than {@code value}.
     * 
     * @param <T>       the array component type.
     * @param array     the array holding the target range.
     * @param fromIndex the lowest index of the array range to process.
     * @param toIndex   the largest index of the array range to process plus
     *                  one.
     * @param value     the target value.
     * @return          the array index.
     */
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

        // Do the exponential search in order to find faster the array subrange
        // that might contain 'value':
        while (bound < rangeLength &&
                array[bound + fromIndex].compareTo(value) < 0) {
            bound <<= 1;
        }

        // The containing range found. Now search in it with binary search:
        return lowerBound(array, 
                          fromIndex + (bound >>> 1), 
                          Math.min(toIndex, fromIndex + bound), 
                          value);
    }

    private static <T extends Comparable<? super T>> 
        int findUpperBound(T[] array, int fromIndex, int toIndex, T value) {
        int bound = 1;
        int rangeLength = toIndex - fromIndex;

        // Do the exponential search in order to find faster the array subrange
        // that might contain 'value':
        while (bound < rangeLength 
                && array[bound + fromIndex].compareTo(value) < 0) {
            bound <<= 1;
        }

        // The containing range found. Now search in it with binary search:
        return upperBound(array, 
                          fromIndex + (bound >>> 1), 
                          Math.min(toIndex, fromIndex + bound),
                          value);
    }
}
