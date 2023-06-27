package com.zyh.ce4j.executor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.zyh.ce4j.domain.ExecutedResult;
import com.zyh.ce4j.domain.Result;
import com.zyh.ce4j.strategy.CheckStrategy;

public class BaseExecutor implements Executor{
	
	private boolean useStdoutStreamGobbler;
	private boolean collectAllOutputStdout;
	private CheckStrategy stdoutCes;
	
	private boolean useErrorStreamGobbler;
	private boolean collectAllOutputError;
	private CheckStrategy errorCes;
	
	private long timeout = 30;
	private TimeUnit timeUnit = TimeUnit.MINUTES;
	
	
	private BaseExecutor(Builder builder) {
		this.useStdoutStreamGobbler = builder.useStdoutStreamGobbler;
		this.collectAllOutputStdout = builder.collectAllOutputStdout;
		this.stdoutCes = builder.stdoutCes;
		
		this.useErrorStreamGobbler = builder.useErrorStreamGobbler;
		this.collectAllOutputError = builder.collectAllOutputError;
		this.errorCes = builder.errorCes;
		
		this.timeout = builder.timeout;
    	this.timeUnit = builder.timeUnit;
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
	
	//采用builder模式进行构建
	public static class Builder {
		//default configure
		private boolean useStdoutStreamGobbler = true;
		private boolean collectAllOutputStdout = false;
		private CheckStrategy stdoutCes = CheckStrategy.NONE_CHECK_STRATEGY_INSTANCE;
		
		private boolean useErrorStreamGobbler = false;
		private boolean collectAllOutputError = false;
		private CheckStrategy errorCes;
        
		private long timeout = 30;
		private TimeUnit timeUnit = TimeUnit.MINUTES;
		
		public class StdoutStreamGobblerBuilder{
			private Builder builder;
			public StdoutStreamGobblerBuilder(Builder builder){
				this.builder = builder;
			}
			
	        public Builder collectAllOutputStdout(boolean collectAllOutputStdout) {
	            this.builder.collectAllOutputStdout = collectAllOutputStdout;
	            return this.builder;
	        }
		}
		
		public class ErrorStreamGobblerBuilder{
			private Builder builder;
			public ErrorStreamGobblerBuilder(Builder builder){
				this.builder = builder;
			}
			
	        public Builder collectAllOutputError(boolean collectAllOutputError) {
	            this.builder.collectAllOutputError = collectAllOutputError;
	            return this.builder;
	        }
		}
		
        public StdoutStreamGobblerBuilder useStdoutStreamGobbler(boolean useStdoutStreamGobbler, CheckStrategy stdoutCes) {
            this.useStdoutStreamGobbler = useStdoutStreamGobbler;
            this.stdoutCes = stdoutCes;
            return new StdoutStreamGobblerBuilder(this);
        }

        public ErrorStreamGobblerBuilder useErrorStreamGobbler(boolean useErrorStreamGobbler, CheckStrategy errorCes) {
            this.useErrorStreamGobbler = useErrorStreamGobbler;
            this.errorCes = errorCes;
            return new ErrorStreamGobblerBuilder(this);
        }
        
        public Builder setTimeout(Long timeout, TimeUnit timeUnit) {
        	if(null != timeout) {
        		this.timeout = timeout;
        	}
        	if(null != timeUnit) {
            	this.timeUnit = timeUnit;
        	}
        	return this;
        }
        
        public BaseExecutor build() {
            return new BaseExecutor(this);
        }
    }

	private void processOutputHandle(final ExecutedResult result,final Process pr, final String comandLine, MutilComandsStream mcs) throws InterruptedException {
		StreamGobbler errorGobbler = null;
    	if(this.useErrorStreamGobbler) {
    		if(mcs == null) {
    			errorGobbler = new StreamGobbler(pr.getErrorStream(), Executor.StreamGobblerType.ERROR,this.collectAllOutputError);
    		}else {
    			errorGobbler = new StreamGobbler(pr.getErrorStream(), Executor.StreamGobblerType.ERROR, mcs, this.collectAllOutputError);
    		}
    		errorGobbler.start();
    	}
    	StreamGobbler stdoutGobbler = null;
    	if(this.useStdoutStreamGobbler) {
    		if(mcs == null) {
    			stdoutGobbler = new StreamGobbler(pr.getInputStream(),Executor.StreamGobblerType.STDOUT, this.collectAllOutputStdout);
    		}else {
    			stdoutGobbler = new StreamGobbler(pr.getInputStream(), Executor.StreamGobblerType.STDOUT, mcs, this.collectAllOutputStdout);
    		}
        	stdoutGobbler.start();
    	}
    	pr.waitFor(this.timeout, this.timeUnit);//等待该命令行进程执行完毕,默认30分钟后自动放弃
    	if(this.useErrorStreamGobbler) {
        	Result res = errorGobbler.exeResult(this.errorCes);
        	result.setStatus(res.getStatus());
        	if(res.getStatus().equals(Result.Status.FAILURE)) {
        		res.setMsg(String.format("execute command line[%s] ,last print: %s, failed: %s", comandLine, stdoutGobbler.getLastPrint(), res.getMsg()));
        	}else if(res.getStatus().equals(Result.Status.UNKNOWN)) {
        		res.setMsg(String.format("execute command line[%s] ,last print: %s", comandLine, stdoutGobbler.getLastPrint()));
        	}
        	result.setErrorResult(res);
    	}
    	if(this.useStdoutStreamGobbler) {
        	Result res = stdoutGobbler.exeResult(this.stdoutCes);
        	result.setStatus(res.getStatus());
        	if(res.getStatus().equals(Result.Status.FAILURE)) {
        		res.setMsg(String.format("execute command line[%s] ,last print: %s, failed: %s", comandLine, stdoutGobbler.getLastPrint(), res.getMsg()));
        	}else if(res.getStatus().equals(Result.Status.UNKNOWN)) {
        		res.setMsg(String.format("execute command line[%s] ,last print: %s", comandLine, stdoutGobbler.getLastPrint()));
        	}
        	result.setStdoutResult(res);
    	}
	}
	
	@Override
	public ExecutedResult execute(String comandLine) {
		ExecutedResult result = new ExecutedResult();
		try {
        	Process pr = Runtime.getRuntime().exec(comandLine);
        	processOutputHandle(result,pr, comandLine, null);
		} catch (IOException | InterruptedException e) {
			result.setStatus(Result.Status.FAILURE);
			result.setErrorResult(new Result(Result.Status.FAILURE, String.format("execute command line[%s] failed: {%s}",comandLine,e.getMessage())));
		}
		return result;
	}
	
	@Override
	public ExecutedResult executeMutilShell(List<String> comandLine) {
		ExecutedResult result = new ExecutedResult();
		if(isWin()) {
			result.setStatus(Result.Status.REJECT);
			result.setErrorResult(new Result(Result.Status.REJECT, "该方法不支持执行win的多命令行，推荐使用[execute(String comandLine)]"));
			return result;
		}
		try {
			Process pr = Runtime.getRuntime().exec("/bin/bash", null, null);
			//多命令行
			MutilComandsStream mcs = new MutilComandsStream(pr.getOutputStream(), comandLine);
			processOutputHandle(result, pr, String.join("&&", comandLine), mcs);
		} catch (IOException | InterruptedException e) {
			result.setErrorResult(new Result(Result.Status.FAILURE, String.format("execute command line[%s] failed: {%s}",comandLine,e.getMessage())));
		}
		return result;
	}

	@Override
	public ExecutedResult execute(String[] cmdarray) {
		ExecutedResult result = new ExecutedResult();
		try {
        	Process pr = Runtime.getRuntime().exec(cmdarray);
        	processOutputHandle(result,pr, String.join(" ", cmdarray), null);
		} catch (IOException | InterruptedException e) {
			result.setStatus(Result.Status.FAILURE);
			result.setErrorResult(new Result(Result.Status.FAILURE, String.format("execute command line[%s] failed: {%s}",String.join(" ", cmdarray),e.getMessage())));
		}
		return result;
	}
}
