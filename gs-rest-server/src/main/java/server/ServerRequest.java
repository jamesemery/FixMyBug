package server;


/* THIS MUST BE IDENTICAL TO ITS COUNTERPART IN THE CLIENT. IF YOU CHANGE ONE, CHANGE BOTH */

/**
 * The object we use to send code to the server.
 */
public class ServerRequest {
	private static String buggy_code;
	private static String error_message;

	public ServerRequest() {}

	public ServerRequest(String buggy_code, String error_message) {
		this.buggy_code = buggy_code;
		this.error_message = error_message;
	}

	public String getBuggyCode() {
		return this.buggy_code;
	}

	public String getErrorMessage() {
		return this.error_message;
	}

	public void setBuggyCode(String buggy_code) {
		this.buggy_code = buggy_code;
	}

	public void setErrorMessage(String error_message) {
		this.error_message = error_message;
	}
}
