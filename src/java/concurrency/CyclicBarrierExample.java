package concurrency;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CyclicBarrierExample {

    public static void main(String[] args) throws InterruptedException {
        concurrency.StableMyCustom<Integer> c = concurrency.StableMyCustom.of();
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
        // Reset barrier to 100 again
        barrier.reset();
        
        // Show result for Stable class result
        System.out.println("RESULT: " + c.value);
    }
}
