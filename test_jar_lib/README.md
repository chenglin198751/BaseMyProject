# ce4j简介
        基于java.lang.Runtime封装的用于简化java调用命令行的工具。
## 起因
	使用java原生库的java.lang.Runtime进行命令行调用时，需要重复编写不少代码。
	并且没有解决潜在的输出流堵塞问题，开箱即用存在一定风险。
## 目标
	基于java.lang.Runtime做封装，内部做了输出流的处理，解决潜在的管道阻塞问题。
	使用者可以忽略输出流的管道阻塞，达到开箱即用的效果。
	本着工具的简单化，封装过程没有使用第三方插件，采用策略模式提供对命令执行结果的判断自定义，
	采用builder模式提供Executor的自定义构建。
# 快速开始
## maven依赖
	<dependency>
		<groupId>com.github.swjuyhz</groupId>
  		<artifactId>ce4j</artifactId>
  		<version>1.2</version>
	</dependency>
# 使用demo [示例](https://github.com/swjuyhz/ce4j/blob/master/src/test/java/com/zyh/comand/ExecuteDemo.java)
#命令行的编写推荐使用命令行构建工具[CommandLineBuilder](https://github.com/swjuyhz/ce4j/blob/master/src/main/java/com/zyh/ce4j/util/CommandLineBuilder.java)
## 默认配置Executor
### 插件默认：使用Stdout输出流，不使用错误输出流，不收集命令行输出，不判定执行结果（即使用空判定策略)
		Executor executor = BaseExecutor.newBuilder().build();
		/*或者（效果同上，此为插件默认配置,完整版）：
		Executor executor3 = BaseExecutor
					.newBuilder()
					.useStdoutStreamGobbler(true, CheckStrategy.NONE_CHECK_STRATEGY_INSTANCE)
					.collectAllOutputStdout(false)
					.build();
		*/
		//window 
		//单条命令行
		if(executor.isWin()) {
			ExecutedResult res = executor.execute("cmd /c dir");
			System.out.println(res);
			//多条命令行
			ExecutedResult res1 = executor.execute("cmd /c C: && cd C:\\Users\\zyh\\Desktop\\learning && dir");
			System.out.println(res1);
		}else {
			//Linux
			//单命令行
			ExecutedResult res2 = executor.execute("ls");
			System.out.println(res2);
			//多命令行
			ExecutedResult res3 = executor.executeMutilShell(Arrays.asList("cd /root/deploy/zyh/bin/poems_generator/", "python3 compose_poem.py 佳"));
			System.out.println(res3);
		}
		
## 自定义配置Executor
### 自定义执行结果判定策略、使用流stdoutStream、收集命令行输出
		Executor executor1 = BaseExecutor
					.newBuilder()
					//使用StdoutStream,使用自定义执行结果判定状态,ColonEndStrategy为插件实现的一种场景的判定策略,
                			//自定义判定策略实现com.zyh.ce4j.strategy.CheckStrategy即可
					.useStdoutStreamGobbler(true, new ColonEndStrategy())
					.collectAllOutputStdout(true)//收集命令行输出
					.build();
		//window 
		//单条命令行
		if(executor1.isWin()) {
			ExecutedResult res = executor1.execute("cmd /c dir");
			System.out.println(res);
			Result stdouResult = res.getStdoutResult();
			if(stdouResult.getStatus() == Result.Status.SUCCESS) {
				//收集的命令行执行的STDOUT输出(Runtime.getRuntime().exec(comandLine).getInputStream())
				List<String> lines = stdouResult.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
			//多条命令行
			ExecutedResult res1 = executor1.execute("cmd /c C: && cd C:\\Users\\zhengyh\\Desktop\\learning && dir");
			System.out.println(res1);
			Result stdouResult1 = res1.getStdoutResult();
			if(stdouResult1.getStatus() == Result.Status.SUCCESS) {
				//收集的命令行执行的STDOUT输出(Runtime.getRuntime().exec(comandLine).getInputStream())
				List<String> lines = stdouResult1.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
		}else {
			//Linux
			//单命令行
			ExecutedResult res2 = executor1.execute("ls");
			System.out.println(res2);
			Result stdouResult = res2.getStdoutResult();
			if(stdouResult.getStatus() == Result.Status.SUCCESS) {
				//收集的命令行执行的STDOUT输出(Runtime.getRuntime().exec(comandLine).getInputStream())
				List<String> lines = stdouResult.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
			//多命令行
			ExecutedResult res3 = executor1.executeMutilShell(Arrays.asList("cd /root/deploy/zyh/bin/poems_generator/", "python3 compose_poem.py 佳"));
			System.out.println(res3);
			Result stdouResult3 = res3.getStdoutResult();
			if(stdouResult.getStatus() == Result.Status.SUCCESS) {
				//收集的命令行执行的STDOUT输出(Runtime.getRuntime().exec(comandLine).getInputStream())
				List<String> lines = stdouResult3.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
		}
### 自定义执行结果判定策略、使用errorStream流、收集命令行errorStream流输出
#### stdoutStream 默认开启，否则可能导致命令行执行堵塞，除非命令行执行后没有该流的输出
		Executor executor2 = BaseExecutor
					.newBuilder()
					//使用错误输出流Runtime.getRuntime().exec(comandLine).getErrorStream()
					.useErrorStreamGobbler(true, new CheckStrategy() {
					    @Override //自定义策略
					    public Result endCheck(String lastPrint) {
					       return new Result(Result.Status.UNKNOWN, "未检查执行结果，执行结果未知。", null);
					    }})
					.collectAllOutputError(true)//收集错误流输出的命令行
					.build();
		//window 
		//单条命令行
		if(executor1.isWin()) {
			ExecutedResult res = executor2.execute("cmd /c dir");
			System.out.println(res);
			Result stdouResult = res.getStdoutResult();
			if(stdouResult.getStatus() == Result.Status.SUCCESS) {
				//收集的命令行执行的STDOUT输出(Runtime.getRuntime().exec(comandLine).getInputStream())
				List<String> lines = stdouResult.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
			
			Result errorResult = res.getErrorResult();
			if(stdouResult.getStatus() == Result.Status.UNKNOWN) {//依据自定义的策略
				//收集的命令行执行的ERROR输出(Runtime.getRuntime().exec(comandLine).getErrorStream())
				List<String> lines = errorResult.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
			
			
			//多条命令行
			ExecutedResult res1 = executor2.execute("cmd /c C: && cd C:\\Users\\zhengyh\\Desktop\\learning && dir");
			System.out.println(res1);
			Result stdouResult1 = res1.getStdoutResult();
			if(stdouResult1.getStatus() == Result.Status.SUCCESS) {
				//收集的命令行执行的STDOUT输出(Runtime.getRuntime().exec(comandLine).getInputStream())
				List<String> lines = stdouResult1.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
			Result errorResult1 = res1.getErrorResult();
			if(stdouResult.getStatus() == Result.Status.UNKNOWN) {//依据自定义的策略
				//收集的命令行执行的ERROR输出(Runtime.getRuntime().exec(comandLine).getErrorStream())
				List<String> lines = errorResult1.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
		}else {
			//Linux
			//单命令行
			ExecutedResult res2 = executor2.execute("ls");
			System.out.println(res2);
			Result stdouResult = res2.getStdoutResult();
			if(stdouResult.getStatus() == Result.Status.SUCCESS) {
				//收集的命令行执行的STDOUT输出(Runtime.getRuntime().exec(comandLine).getInputStream())
				List<String> lines = stdouResult.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
			Result errorResult1 = res2.getErrorResult();
			if(stdouResult.getStatus() == Result.Status.UNKNOWN) {//依据自定义的策略
				//收集的命令行执行的ERROR输出(Runtime.getRuntime().exec(comandLine).getErrorStream())
				List<String> lines = errorResult1.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
			//多命令行
			ExecutedResult res3 = executor2.executeMutilShell(Arrays.asList("cd /root/deploy/zyh/bin/poems_generator/", "python3 compose_poem.py 佳"));
			System.out.println(res3);
			Result stdouResult3 = res3.getStdoutResult();
			if(stdouResult.getStatus() == Result.Status.SUCCESS) {
				//收集的命令行执行的STDOUT输出(Runtime.getRuntime().exec(comandLine).getInputStream())
				List<String> lines = stdouResult3.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
			Result errorResult2 = res3.getErrorResult();
			if(stdouResult.getStatus() == Result.Status.UNKNOWN) {//依据自定义的策略
				//收集的命令行执行的ERROR输出(Runtime.getRuntime().exec(comandLine).getErrorStream())
				List<String> lines = errorResult2.getData();
				for(String line:lines) {
					System.out.println(line);
				}
			}
		}
