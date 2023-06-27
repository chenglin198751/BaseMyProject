package cmd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public interface Executor {
	
	/**
	 * 命令行执行，注：Linux下的多命令行，请使用[executeMutilShell(List<String> comandLine)]
	 * @param comandLine 命令行
	 * @param ces 判定命名行执行成功与否的策略
	 * @param collectAllOutput :是否收集命令行输出的结果
	 * @return
	 */
	public ExecutedResult execute(String comandLine);
	
	/**
	 * 以数组方式组织命令行，推荐当命令行中文件存在空格等
	 * @param cmdarray
	 * @return
	 */
	public ExecutedResult execute(String[] cmdarray);
	
	/**
	 * 该方法只支持Linux下shell多命令行，win多命令行使用[execute(String comandLine)]即可。
	 * @param comandLine 命令行
	 * @param ces 判定命名行执行成功与否的策略
	 * @return
	 */
	public ExecutedResult executeMutilShell(List<String> comandLine);
	
	
	default public boolean isWin() {
		return System.getProperty("os.name").toLowerCase().startsWith("win");
	}
	
	
	enum StreamGobblerType{
		STDOUT,ERROR
	}
	
	class MutilComandsStream{
		private PrintWriter pw;
		private List<String> commands;
		public MutilComandsStream(OutputStream os, List<String> commands) {
			super();
			this.pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
			this.commands = commands;
		}
		public PrintWriter getPw() {
			return pw;
		}
		public void setPw(PrintWriter pw) {
			this.pw = pw;
		}
		public List<String> getCommands() {
			return commands;
		}
		public void setCommands(List<String> commands) {
			this.commands = commands;
		}
	}
	
	/**
	 * 用于处理Runtime.getRuntime().exec产生的错误流及输出流
	 * 不处理会产生阻塞现象
	 */
	class StreamGobbler extends Thread {
		private List<String> outLines;
		private InputStream is;
		private StreamGobblerType type;
		private MutilComandsStream mcs;
		private String lastPrint;
		private boolean collectAllOutput;
		    
		public StreamGobbler(InputStream is, StreamGobblerType type, boolean collectAllOutput) {
			this(is, type, null, collectAllOutput);
		}
		/**
		 * 
		 * @param is
		 * @param type
		 * @param redirect 
		 */
	    public StreamGobbler(InputStream is, StreamGobblerType type, MutilComandsStream mcs, boolean collectAllOutput) {
	        this.is = is;
	        this.type = type;
	        this.mcs = mcs;
	        this.collectAllOutput = collectAllOutput;
	        if(this.collectAllOutput) {
	        	this.outLines = new ArrayList<String>();//命令行输出结果
	        }
	    }
	    
	    public String getLastPrint() {
	    	return this.lastPrint;
	    }
	    
	    public List<String> getOutputLines() {
	    	return this.outLines == null ? new ArrayList<>() : this.outLines;
	    }
	    
	    /**
	     * 命令行是否成功执行完毕
	     * @return
	     */
	    public Result exeResult(CheckStrategy ces) {
	    	Result er = ces.endCheck(lastPrint);
	    	if(collectAllOutput) {
	    		er.setData(outLines);
	    	}
	    	return er;
	    }
	    
	    public void run() {
	        InputStreamReader isr = null;
	        BufferedReader br = null;
	        PrintWriter pw = null;
	        //运行多行的命令
	        if(mcs != null && mcs.getPw() != null) {
	        	pw = mcs.getPw();
	            for (String line : mcs.getCommands()) {
	            	pw.println(line);
	            }
	            pw.println("exit");// 这个命令必须执行，否则in流不结束。
	        }
	        try {
	            isr = new InputStreamReader(is);
	            br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null) {
	            	if(collectAllOutput) { 
	            		outLines.add(line); //搜集所有输出
	            	}
	            	switch(type) {
	                case ERROR:
	                	lastPrint = line;
	                	System.out.println("ERROR->" + line);
	                	break;
	                case STDOUT:
	                	lastPrint = line;
	                	System.out.println("STDOUT->" + line);
	                	break;
	                default:
	                	lastPrint = line;
	                	System.out.println("OTHER->" + line);
	                	break;
	                }
	            }
	            
	        } catch (IOException ioe) {
	            ioe.printStackTrace();  
	        } finally{
	        	if (pw != null) {
		        	pw.close();
	        	}
	        	try {
					br.close();
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    }
	}
}
