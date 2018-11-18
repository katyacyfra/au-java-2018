import java.util.LinkedList;

public class ThreadSafeQueue<T> {

    private LinkedList<T> queue = new LinkedList<>();

    public synchronized T dequeue() {
        T task = null;
        while (queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                return task;
            }
        }
        task = queue.remove();
        return task;
    }

    public synchronized void enqueue(T item) {
        queue.add(item);
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }


}
