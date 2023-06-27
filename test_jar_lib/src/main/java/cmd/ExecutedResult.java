package cmd;

public class ExecutedResult {

	private Result.Status status = Result.Status.SUCCESS;//默认为成功
	private Result stdoutResult;
	private Result errorResult;
	
	public Result.Status getStatus() {
		return status;
	}
	public void setStatus(Result.Status status) {
		this.status = status;
	}
	public Result getStdoutResult() {
		return stdoutResult;
	}
	public void setStdoutResult(Result stdoutResult) {
		this.stdoutResult = stdoutResult;
	}
	public Result getErrorResult() {
		return errorResult;
	}
	public void setErrorResult(Result errorResult) {
		this.errorResult = errorResult;
	}
	@Override
	public String toString() {
		return "ExecutedResult [status=" + status + ", stdoutResult=" + stdoutResult + ", errorResult=" + errorResult
				+ "]";
	}

}
