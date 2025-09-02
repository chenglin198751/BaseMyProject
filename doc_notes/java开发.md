## java开发：

**1、CompletableFuture用法：** 

    1、使用CompletableFuture.allOf() 并发执行耗时任务（同时执行不分先后）：
    CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
        // 异步耗时任务1：
        List<File> fileList = new ArrayList<File>();
        FileUtils.getAllFiles(new File("E:\\apks"), fileList);
        System.out.println("11-fileList.size(): " + fileList.size());
    });
    CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
        // 异步耗时任务2：
        List<File> fileList = new ArrayList<File>();
        FileUtils.getAllFiles(new File("E:\\apks"), fileList);
        System.out.println("22-fileList.size(): " + fileList.size());
    });
    try {
        // 并发执行耗时任务1和2：
        CompletableFuture.allOf(future1, future2).get();
    } catch (Exception e) {
        e.printStackTrace();
    }

    2、使用thenRun()逐个执行耗时任务：
    CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
        List<File> fileList = new ArrayList<>();
        FileUtils.getAllFiles(new File("E:\\apks"), fileList);
        System.out.println("11-fileList.size(): " + fileList.size());
    });
    CompletableFuture<Void> future2 = future1.thenRun(() -> {
        List<File> fileList = new ArrayList<>();
        FileUtils.getAllFiles(new File("E:\\apks"), fileList);
        System.out.println("22-fileList.size(): " + fileList.size());
    });
    future2.join();

    3、每个耗时任务执行完的回调：
    future1.whenComplete((result, ex) -> {
        if (ex == null) {
            System.out.println("future1 completed successfully.");
        } else {
            System.out.println("future1 failed: " + ex.getMessage());
        }
    });
    future2.whenComplete((result, ex) -> {
        if (ex == null) {
            System.out.println("future2 completed successfully.");
        } else {
            System.out.println("future2 failed: " + ex.getMessage());
        }
    });

**2、线程池用法：**

    // 核心线程设为10个，最大为20个，空闲线程等待时间为30秒(30秒没被使用，会被系统回收)
    public static Executor mExecutor = new ThreadPoolExecutor(10, 20, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

**3、CountDownLatch用法：**
    
    1.CountDownLatch 是 Java 并发包（java.util.concurrent）中的一个同步辅助类。它常用于让一个或多个线程等待，直到在其他线程中执行的一组操作完成。
    2.CountDownLatch(int count)：初始化时设置计数器的值（一般等于需要等待的线程数）。
    3.await()：调用此方法的线程会被阻塞，直到计数器变为0。
    4.countDown()：每调用一次，计数器减1。当计数器变为0时，所有在await()上等待的线程会被唤醒。

    public class Example {
    public static void main(String[] args) throws InterruptedException {
    int threadCount = 3;
    CountDownLatch latch = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        System.out.println(Thread.currentThread().getName() + " 正在执行");
                        Thread.sleep(1000); // 模拟任务耗时
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();  // 任务完成，计数器减1
                    }
                }).start();
            }
    
            // 主线程等待所有子线程完成
            latch.await();
            System.out.println("所有线程执行完毕，主线程继续...");
        }
    }    