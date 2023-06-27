package com.zyh.ce4j.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandLineBuilder {
	private CommandLineBuilder() {}

	public static CmdBuilder win() {
		return new CmdBuilder();
	}
	
	public static ShellBuilder linux() {
		return new ShellBuilder();
	}
	
	/**
	 * window cmd builder
	 * @author zyh
	 *
	 */
	public static class CmdBuilder{
		/*
		 *统一规则： 
		 * 命令行的所有key和参数都加双引号--->解决输入参数带空格被截断的问题
		 */
		private String and = "&&";
		private List<String> cmds;
		private CmdBuilder(){
			cmds = new ArrayList<>(Arrays.asList("cmd","/c"));
		}
		
		private void fixAnd() {
			if(!cmds.isEmpty()){
				String last = cmds.get(cmds.size() - 1);
				if(!last.equals("/c") && !last.equals(and)) {
					cmds.add(and);
				}
			}
		}
		
		public CmdBuilder cd(String directory) {
			fixAnd();
			String[] dirs = directory.split(":");
			if(dirs != null && dirs.length >= 2) {
				cmds.add(dirs[0].toUpperCase()+":");
				cmds.add(and);
			}
			cmds.add("cd");
			cmds.add("\""+directory+"\"");
			return this;
		}
		
		public CmdBuilder dir() {
			fixAnd();
			cmds.add("dir");
			return this;
		}
		
		//another command: TO DO 
		
		/**
		 * 自行制定命令及其参数
		 * @param goal
		 * @param arguments
		 * @return
		 */
		public CmdBuilder goal(String goal, String... arguments) {
			fixAnd();
			cmds.add(goal);
			if(null != arguments && arguments.length > 0) {
				cmds.addAll(Stream.of(arguments).map(a->"\"" + a + "\"").collect(Collectors.toList()));
			}
			return this;
		}
		
		@Override
		public String toString() {
			return String.join(" ", cmds);
		}
		
		public String[] toArray() {
			return cmds.toArray(new String[cmds.size()]);
		}
		
	}
	
	/**
	 * Linux shell builder
	 * @author zyh
	 *
	 */
	public static class ShellBuilder{
		/*
		 *统一规则： 
		 * 命令行的所有key和参数都加双引号--->解决输入参数带空格被截断的问题
		 */
		private List<String> cmds;
		private ShellBuilder(){
			cmds = new ArrayList<>();
		}
		
		public ShellBuilder cd(String dir) {
			cmds.add("cd " + "\"" + dir + "\"");
			return this;
		}
		
		public ShellBuilder ls() {
			cmds.add("ls");
			return this;
		}
		
		public ShellBuilder chmod(String filePath, int privilegeCode) {
			cmds.add("chmod " + privilegeCode + " \"" + filePath + "\"");
			return this;
		}
		
		//another command: TO DO 
		
		/**
		 * 自行制定命令及其参数，例子：
		 * goal("node", "index.js", "--md", "### **请在这里编辑正文**", "--title", "主要功能"," --out-pdf", "/root/PPDC/files/download/54/1/20200717134739374/主要功能222.pdf")
		 * 输出： node "index.js" "--md" "### **请在这里编辑正文**" "--title" "主要功能" "--out-pdf" "/root/PPDC/files/download/54/1/20200717134739374/主要功能222.pdf"
		 * 通过全局加双引号解决空格截断的问题
		 * @param goal 
		 * @param arguments
		 * @return
		 */
		public ShellBuilder goal(String goal, String... arguments) {
			StringBuilder sb = new StringBuilder(goal);
			if(null != arguments && arguments.length > 0) {
				for(String arg : arguments) {
					sb.append(" ").append("\"").append(arg).append("\"");
				}
			}
			cmds.add(sb.toString());
			return this;
		}
		
		/**
		 * The result of this method just be used for ce4j. ce4j see: https://github.com/swjuyhz/ce4j
		 * @return
		 */
		public List<String> toList() {
			return cmds;
		}
		
		@Override
		public String toString() {
			return String.join(" && ", cmds);
		}
	}
	
	public static void main(String[] args) {
		String cmd = CommandLineBuilder.win().cd("C:\\Users\\zyh\\Desktop\\progress").dir().cd("D:\\360").dir().goal("node", "index.js", "--md", "### **请在这里编辑正文**", "--title", "主要功能"," --out-pdf", "/root/PPDC/files/download/54/1/20200717134739374/主要功能222.pdf").goal("node", "index.js", "--md", "### **请在这里编辑正文**", "--title", "主要功能"," --out-pdf", "/root/PPDC/files/download/54/1/20200717134739374/主要功能222.pdf").toString();
		String[] cmds = CommandLineBuilder.win().cd("C:\\Users\\zyh\\Desktop\\progress").dir().cd("D:\\360").dir().goal("node", "index.js", "--md", "### **请在这里编辑正文**", "--title", "主要功能"," --out-pdf", "/root/PPDC/files/download/54/1/20200717134739374/主要功能222.pdf").toArray();
		System.out.println("WIN-------------: " + cmd);
		System.out.println("WIN-------------: " + cmds);
		String s = CommandLineBuilder.linux().goal("node", "index.js", "--md", "### **请在这里编辑正文**", "--title", "主要功能"," --out-pdf", "/root/PPDC/files/download/54/1/20200717134739374/主要功能222.pdf").goal("node", "index.js", "--md", "### **请在这里编辑正文**", "--title", "主要功能"," --out-pdf", "/root/PPDC/files/download/54/1/20200717134739374/主要功能222.pdf")
								  .toString();
		System.out.println("Linux-------------: " + s);
		List<String> ss = CommandLineBuilder.linux().goal("node", "index.js", "--md", "### **请在这里编辑正文**", "--title", "主要功能"," --out-pdf", "/root/PPDC/files/download/54/1/20200717134739374/主要功能222.pdf")
				  .toList();
		System.out.println("Linux-------------: " + ss.get(0));
	}
}
