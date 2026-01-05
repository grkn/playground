package playground.concurrency;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class CyclicBarrierExample {

    public static void main(String[] args) throws InterruptedException {
        StableMyCustom<Integer> c = StableMyCustom.of();
        // 100 threads in thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        // wait 100 threads for to release
        CyclicBarrier barrier = new CyclicBarrier(100);
        for (int i = 0; i < 100; i++) {
            final int index = i;

            executorService.submit(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(index);
                c.orElseSet(() -> index);
            });
        }
        // wait for barrier to finish collecting 100 threads
        while (barrier.getNumberWaiting() != 0) {
            Thread.sleep(100);
        }

        // Close thread pool
        executorService.shutdown();

        // Show result for Stable class result
        System.out.println("RESULT: " + c.value);
    }

    static class StableMyCustom<T> {

        T value = null;

        private StableMyCustom() {
        }

        public static <T> StableMyCustom<T> of() {
            return new StableMyCustom<>();
        }

        // concurrent access to set operation can cause errors. Use synchronized for same instance
        public synchronized T orElseSet(Supplier<T> lambda) {
            // first set operation and value is null
            if (value == null) {
                value = lambda.get(); // set operation
                return value;
            }
            // return rest but not set operation executed
            return value;
        }
    }
}
