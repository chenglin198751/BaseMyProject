package cmd;

import java.util.List;

public class Result{
	public static enum Status{
		/**执行成功**/
		SUCCESS,
		/**执行失败**/
		FAILURE,
		/**拒绝执行**/
		REJECT,
		/**不知道执行结果**/
		UNKNOWN;
	}
	
	
	private Status status;
	private String msg;
	private List<String> data; //收集的命令行运行的所有输出
	public Result() {
		super();
	}
	public Result(Status status, String msg) {
		super();
		this.status = status;
		this.msg = msg;
	}
	
	
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public List<String> getData() {
		return data;
	}
	public void setData(List<String> data) {
		this.data = data;
	}
}
