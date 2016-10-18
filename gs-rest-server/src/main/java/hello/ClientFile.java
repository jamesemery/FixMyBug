package hello;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientFile {
	private static String fileContent;
	private static String errorMessage;

	// @JsonCreator
	// public ClientFile(@JsonProperty("fileContent") String file, @JsonProperty("errorMessage") String errorMessage) {
	// 	this.fileContent = file;
	// 	this.errorMessage = errorMessage;
	// }
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
