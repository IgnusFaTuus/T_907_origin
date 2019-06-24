package net.kehui.www.threadtest;

/**
 * net.kehui.www.threadtest
 *
 * @author IF
 * @date 2019/5/20
 */
public class TestThreadPool {
    public static void main(String[] args) {
        //创建3个线程的线程池
        ThreadPool t = ThreadPool.getThreadPool(3);
        t.execute(new Runnable[]{new Task(), new Task(), new Task()});
        t.execute(new Runnable[]{new Task(), new Task(), new Task()});
        System.out.println(t);
        //所有线程都执行完才destroy
        t.destroy();
        System.out.println(t);
    }

    /**
     * 任务类
     */
    static class Task implements Runnable {
        private static volatile int i = 1;

        @Override
        public void run() {
            System.out.println("任务" + (i++) + "完成");
        }
    }

}
