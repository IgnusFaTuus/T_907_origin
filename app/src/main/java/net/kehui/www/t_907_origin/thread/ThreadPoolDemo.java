package net.kehui.www.t_907_origin.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author li.md
 * @date 2019/6/26
 */
public class ThreadPoolDemo {

    /**
     * 定义静态内部线程类
     */
    public static class MyTask implements Runnable {

        @Override
        public void run() {
            System.out.println(System.currentTimeMillis() + ":Thread name:"
                    + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        MyTask myTask = new MyTask();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("demo-pool-%d").build();
        ExecutorService executorService = new ThreadPoolExecutor(5, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < 10; i++) {
            executorService.execute(myTask);
        }
        executorService.shutdown();
    }
}
