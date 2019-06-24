package net.kehui.www.threadtest;

import java.util.LinkedList;
import java.util.List;

/**
 * net.kehui.www.threadtest
 *
 * @author IF
 * @date 2019/5/20
 */
public final class ThreadPool {

    /**
     * 线程池中默认线程的个数为5
     */
    private static int workerNum = 5;

    /**
     * 工作线程
     */
    private WorkThread[] workThreads;

    /**
     * 未处理的任务
     */
    private static volatile int finished_task = 0;

    /**
     * 任务队列，作为一个缓冲，List线程不安全
     */
    private List<Runnable> taskQueue = new LinkedList<Runnable>();
    private static ThreadPool threadPool;

    /**
     * 创建具有默认线程个数的线程池
     */
    private ThreadPool() {
        this(5);
    }

    /**
     * 创建线程池，worker_num为线程池中工作线程的个数
     */
    private ThreadPool(int workerNum) {
        ThreadPool.workerNum = workerNum;
        workThreads = new WorkThread[workerNum];
        for (int i = 0; i < workerNum; i++) {
            workThreads[i] = new WorkThread();
            //开启线程池中的线程
            workThreads[i].start();
        }
    }

    /**
     * 单态模式，获得一个默认线程个数的线程池
     */
    public static ThreadPool getThreadPool() {
        return getThreadPool(ThreadPool.workerNum);
    }

    /**
     * @param workerNum1
     * @return
     *
     * 单态模式，获得一个指定线程个数的线程池，workerNum(>0)为线程池中工作线程的个数
     * workerNum <=0 创建默认的工作线程个数
     */
    public static ThreadPool getThreadPool(int workerNum1) {
        if (workerNum1 <= 0) {
            workerNum1 = ThreadPool.workerNum;
        }
        if (threadPool == null) {
            threadPool = new ThreadPool(workerNum1);
        }
        return threadPool;
    }

    /**
     * 执行任务，把任务加入任务队列，什么时候执行由线程管理器决定
     */
    public void execute(Runnable task) {
        synchronized (taskQueue) {
            taskQueue.add(task);
            taskQueue.notify();
        }
    }

    /**
     * 批量执行任务，把任务加入任务队列，什么时候执行由线程管理器决定
     */
    public void execute(Runnable[] task) {
        synchronized (taskQueue) {
            for (Runnable t : task) {
                taskQueue.add(t);
                taskQueue.notify();
            }
        }
    }

    /**
     * 销毁线程池，该方法保证在所有任务都完成的情况下才销毁所有线程，否则等待任务完成才销毁
     */
    public void destroy() {
        while (!taskQueue.isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //工作线程停止工作，且置为null
        for (int i = 0; i < workerNum; i++) {
            workThreads[i].stopWorker();
            workThreads[i] = null;
        }
        threadPool = null;
        //清空任务队列
        taskQueue.clear();
    }

    /**
     * 返回工作线程的个数
     */
    public int getWorkThreadNumber() {
        return workerNum;
    }

    /**
     * 返回已完成任务的个数，这里的已完成只是出了任务队列的任务个数
     * 可能该任务并没有实际执行
     */
    public int getFinishedTaskNumber() {
        return finished_task;
    }

    /**
     * 返回任务队列的长度，即还没处理的任务个数
     */
    public int getWaitTaskNumber() {
        return taskQueue.size();
    }

    /**
     * 覆盖toString方法，返回线程池信息，工作线程个数和已完成任务个数
     */
    @Override
    public String toString() {
        return "WorkThread number:" + workerNum + "finished task number:" + finished_task +
                "wait task number:" + getWaitTaskNumber();
    }

    /**
     * 内部类，工作线程
     */
    private class WorkThread extends Thread {
        /**
         * 该工作线程是否有效，用于结束该工作线程
         */
        private boolean isRunning = true;

        /**
         * 如果任务队列不空，则取出任务执行，否则任务队列空，则等待
         */
        @Override
        public void run() {
            Runnable r = null;
            while (isRunning) {
                synchronized (taskQueue) {
                    while (isRunning && taskQueue.isEmpty()) {
                        try {
                            taskQueue.wait(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!taskQueue.isEmpty()) {
                        r = taskQueue.remove(0);
                    }
                    if (r != null) {
                        r.run();
                    }
                    finished_task++;
                    r = null;
                }
            }
        }

        /**
         * 停止工作，让该线程自然执行完run方法，自然结束
         */
        public void stopWorker() {
            isRunning = false;
        }
    }
}
