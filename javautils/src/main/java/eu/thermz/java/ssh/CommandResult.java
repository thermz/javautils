package eu.thermz.java.ssh;

public class CommandResult {
	private int retcode;
	private String output;

	public CommandResult() {

	}

	public CommandResult(int retcode, String output) {
		this.retcode = retcode;
		this.output = output;
	}

	public int getRetcode() {
		return retcode;
	}

	public void setRetcode(int retcode) {
		this.retcode = retcode;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}
}
