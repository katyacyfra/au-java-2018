
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadPool {
    private final int threadNumber;
    private final ThreadSafeQueue<LightFutureImpl> queue = new ThreadSafeQueue<>();
    private final Worker[] threads;


    ThreadPool(int nThreads) {
        threadNumber = nThreads;
        threads = new Worker[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            threads[i] = new Worker();
            threads[i].start();
        }

    }

    public <T> LightFuture<T> execute(Supplier<T> supplier) {
        LightFutureImpl<T> task = new LightFutureImpl<>(supplier);
        synchronized (queue) {
            queue.enqueue(task);
            queue.notify();
        }
        return task;
    }

    public void shutdown() {
        for (Worker w : threads) {
            w.interrupt();
        }
    }

    private class Worker extends Thread {
        public void run() {
            LightFutureImpl task;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    task = queue.dequeue();
                }
                task.execute();

            }
        }
    }

    public class LightFutureImpl<T> implements LightFuture<T> {
        private boolean ready = false;
        private final Supplier<T> supplier;
        private T result;
        private Exception supplierException = null;

        LightFutureImpl(@NotNull Supplier<T> suppl) {
            supplier = suppl;
        }

        @Override
        public boolean isReady() {
            return ready;
        }


        private synchronized void execute() {
            try {
                result = supplier.get();
            } catch (Exception e) { //exception occured in supplier
                supplierException = e;
            }
            ready = true;
            notifyAll();
        }

        @Override
        public T get() throws LightExecutionException {
            if (!ready) {
                try {
                    synchronized (this) {
                        while (!ready) {
                            wait();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (supplierException != null) {
                    supplierException.printStackTrace();
                    throw new LightExecutionException();
                }
            }
            return result;
        }


        @Override
        public synchronized <S> LightFuture<S> thenApply(Function<T, S> f) {
            if (!ready) {
                try {
                    get();
                } catch (LightExecutionException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            return ThreadPool.this.execute(() -> f.apply(result));
        }
    }
}


