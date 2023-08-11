
## CmdTask使用方法：
    CmdTask task = new CmdTask("java -jar E:\\AndroidCode\\xxx.jar");
    CmdTask.Outs outs = task.run(false);

    //执行过程中同步获取输出日志：
    task.setLogListener(new OnLogListener<String>() {
        @Override
        public void onFinished(String line) {
            System.out.println(line);
        }
    });

    //执行结束才能获取输出的命令行日志：
    for (String str:outs.getInputList()){
        System.out.println(str);
    }

    //执行结束才能获取错误日志：
    for (String str:outs.getErrorList()){
        System.out.println(str);
    }
