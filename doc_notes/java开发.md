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