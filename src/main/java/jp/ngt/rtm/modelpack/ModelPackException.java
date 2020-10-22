package jp.ngt.rtm.modelpack;

public class ModelPackException extends RuntimeException {
	public ModelPackException(Throwable cause) {
		super(cause);
	}

	public ModelPackException(String message, String fileName, Throwable cause) {
		super(message + " (" + fileName + ")", cause);
	}

	public ModelPackException(String message, String fileName) {
		this(message, fileName, null);
	}
}