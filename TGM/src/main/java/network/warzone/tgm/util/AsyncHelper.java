package network.warzone.tgm.util;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Created by chatasma on 02/02/2021
 */
public class AsyncHelper {

    public static void doAsync(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static void taskQueue(Collection<Runnable> tasks, @Nullable Runnable callback) {
        taskQueueWithLock(tasks, null, callback);
    }

    public static void taskQueueWithLock(Collection<Runnable> tasks, @Nullable ReentrantLock lock, @Nullable Runnable callback) {
        doAsync(() -> {
            if (lock != null) lock.lock();
            for (Runnable task : tasks) {
                task.run();
            }
            if (callback != null) callback.run();
        });
    }
}
