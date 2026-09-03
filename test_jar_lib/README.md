## CmdTask 使用方法：

    // 执行命令（execute 参数为是否实时打印输出）
    CmdTask task = new CmdTask(new String[]{"java", "-jar", "E:\\AndroidCode\\xxx.jar"});
    CmdTask.Result result = task.execute(false);

    // 是否执行成功（无异常且退出码为 0）
    if (result.isSuccess()) {
        // 命令合并输出（stdout + stderr）
        for (String line : result.getOutput()) {
            System.out.println(line);
        }
    } else {
        // 失败原因：执行异常用 getError()，非零退出码用 getExitValue()
        System.err.println(result.getError());
    }