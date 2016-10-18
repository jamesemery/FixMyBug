package client;

public class ClientFile {
	private static String fileContent;
	private static String errorMessage;

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
}
