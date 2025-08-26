package com.example;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CounterWithReadWriteLock {
    private int count = 0;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public void increment() {
        writeLock.lock();
        try {
            count++;
            System.out.println(Thread.currentThread().getName() + ": Incremented to " + count);
            Thread.sleep(1); // Simulate work to increase contention
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + ": Interrupted during increment");
        } finally {
            writeLock.unlock();
        }
    }

    public int getCount() {
        readLock.lock();
        try {
            return count;
        } finally {
            readLock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CounterWithReadWriteLock counter = new CounterWithReadWriteLock();
        System.out.println("Initial count: " + counter.getCount());

        // Writer tasks
        Runnable writeTask = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        };

        // Reader tasks
        Runnable readTask = () -> {
            for (int i = 0; i < 100; i++) {
                int value = counter.getCount();
                System.out.println(Thread.currentThread().getName() + ": Read count = " + value);
                try {
                    Thread.sleep(1); // Simulate work
                } catch (InterruptedException e) {
                    System.out.println(Thread.currentThread().getName() + ": Interrupted during read");
                }
            }
        };

        // Create threads
        Thread w1 = new Thread(writeTask, "Writer-1");
        Thread w2 = new Thread(writeTask, "Writer-2");
        Thread r1 = new Thread(readTask, "Reader-1");
        Thread r2 = new Thread(readTask, "Reader-2");
        Thread r3 = new Thread(readTask, "Reader-3");
        Thread r4 = new Thread(readTask, "Reader-4");

        // Start threads
        w1.start();
        w2.start();
        r1.start();
        r2.start();
        r3.start();
        r4.start();

        // Wait for completion
        w1.join();
        w2.join();
        r1.join();
        r2.join();
        r3.join();
        r4.join();

        System.out.println("Final count: " + counter.getCount()); // Should be 2000
    }
}