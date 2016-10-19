package client;

/*
* ClientFile.java
*
* A simple class that contains a java file and its associated
* compiler-error message.
*/
public class ClientFile {
	private static String fileContent;
	private static String errorMessage;

	public ClientFile() {}

	public ClientFile(String file, String errorMessage) {
		this.fileContent = file;
		this.errorMessage = errorMessage;
	}

	public String getFileContent() {
		return this.fileContent;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public void setFileContent(String content) {
		this.fileContent = content;
	}

	public void setErrorMessage(String error) {
		this.errorMessage = error;
	}
}
