
## CmdTask使用方法：
    CmdTask task = new CmdTask("java -jar E:\\AndroidCode\\xxx.jar");
    CmdTask.Outs outs = task.run(false);

    //读取输出的命令行日志：
    for (String str:outs.getInputList()){
        System.out.println(str);
    }

    //读取错误日志：
    for (String str:outs.getErrorList()){
        System.out.println(str);
    }
