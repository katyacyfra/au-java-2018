
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThreadPoolTest {

    @Test
    public void testSingleThread() throws LightExecutionException {
        ThreadPool th = new ThreadPool(1);
        LightFuture<Integer> task = th.execute(() -> 2 * 2);
        assertFalse(task.isReady());
        assertEquals((Integer) 4, task.get());
        assertTrue(task.isReady());
        assertEquals((Integer) 4, task.get());

    }

    @Test
    public void testSimple() throws LightExecutionException {
        ThreadPool th = new ThreadPool(2);
        LightFuture<Integer> task = th.execute(() -> 2);
        LightFuture<Integer> task2 = th.execute(() -> 2 * 2);
        LightFuture<Integer> task3 = th.execute(() -> 3 * 2);
        LightFuture<Integer> task4 = th.execute(() -> 4 * 2);
        assertEquals((Integer) 2, task.get());
        assertEquals((Integer) 4, task2.get());
        assertEquals((Integer) 6, task3.get());
        assertEquals((Integer) 8, task4.get());
    }

    @Test
    public void testThenApply() throws LightExecutionException {
        ThreadPool th = new ThreadPool(4);
        List<LightFuture<Double>> after = new ArrayList<>();
        int i = 0;
        while (i < 10) {
            final int j = i;
            LightFuture<Integer> task = th.execute(() -> j);
            after.add(task.thenApply(x -> pow(x, j)));
            i++;
        }
        i = 0;
        while (i < 10) {
            assertEquals((Double) pow(i, i), after.get(i).get());
            i++;
        }

    }

    @Test
    public void testLongTasks() throws LightExecutionException {
        ThreadPool th = new ThreadPool(4);
        List<LightFuture<Integer>> tasks = new ArrayList<>();
        int i = 0;
        while (i < 10) {
            tasks.add(th.execute(() -> {
                try {
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 2;
            }));
            i++;
        }
        i = 0;
        while (i < 10) {
            assertEquals((Integer) 2, tasks.get(i).get());
            i++;
        }

    }

    @Test
    public void testLongThenApply() throws LightExecutionException {
        ThreadPool th = new ThreadPool(8);
        List<LightFuture<Double>> after = new ArrayList<>();
        int i = 0;
        while (i < 10) {
            final int j = i;
            LightFuture<Integer> task = th.execute(() -> {
                try {
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 2;
            });
            after.add(task.thenApply(x -> pow(x, j)));
            i++;
        }
        i = 0;
        while (i < 10) {
            assertEquals((Double) pow(2, i), after.get(i).get());
            i++;
        }

    }

    @Test(expected = LightExecutionException.class)
    public void testLightFutureException() throws LightExecutionException {
        ThreadPool th = new ThreadPool(2);
        LightFuture<Integer> task = th.execute(() -> 2 / 0);
        task.get();


    }

    @Test
    public void testShudown() {
        ThreadPool th = new ThreadPool(4);

        LightFuture<Integer> task = th.execute(() -> {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        });

        LightFuture<Integer> task2 = th.execute(() -> {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        });

        th.shutdown();
        assertFalse(task.isReady());
        assertFalse(task2.isReady());

    }


    @Test
    public void testCountActiveThreads() throws LightExecutionException {
        int threadsBefore = Thread.activeCount();
        ThreadPool th = new ThreadPool(6);
        assertEquals(threadsBefore + 6, Thread.activeCount());
        LightFuture<Integer> task = th.execute(() -> {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        });
        assertEquals(threadsBefore + 6, Thread.activeCount());

        LightFuture<Integer> task2 = th.execute(() -> {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        });
        task.get();
        task2.get();
        assertEquals(threadsBefore + 6, Thread.activeCount());
    }

}
