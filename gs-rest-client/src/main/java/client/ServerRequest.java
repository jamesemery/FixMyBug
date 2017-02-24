package client;

/* THIS MUST BE IDENTICAL TO ITS COUNTERPART IN THE SERVER. IF YOU CHANGE ONE, CHANGE BOTH */

/**
 * The object we use to send code to the server.
 */
public class ServerRequest {
	private static String buggy_code;
	private static String index_size;

	public ServerRequest() {}

	public ServerRequest(String buggy_code) {
		this.buggy_code = buggy_code;
	}

	public ServerRequest(String buggy_code, String index_size) {
		this.buggy_code = buggy_code;
		this.index_size = index_size;
	}

	public String getBuggyCode() {
		return this.buggy_code;
	}

	public String getErrorMessage() {
		return this.index_size;
	}

	public void setBuggyCode(String buggy_code) {
		this.buggy_code = buggy_code;
	}

	public void setErrorMessage(String index_size) {
		this.index_size = index_size;
	}
}
