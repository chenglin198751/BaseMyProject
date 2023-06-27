
## CmdTask使用方法：
    CmdTask task = new CmdTask("java -jar E:\\AndroidCode\\xxx.jar");
    CmdTask.Outs outs = task.run(false);

    for (String str:outs.getInputList()){
        System.out.println(str);
    }

    for (String str:outs.getErrorList()){
        System.out.println(str);
    }
