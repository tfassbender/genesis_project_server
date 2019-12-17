package net.jfabricationgames.genesis_project_server.exception;

public class GameDataException extends Exception {
	
	private static final long serialVersionUID = 993873979940297184L;
	
	private Cause cause;
	
	public GameDataException(String message, Throwable throwable, Cause cause) {
		super(message, throwable);
		this.cause = cause;
	}
	
	public GameDataException(String message, Cause cause) {
		super(message);
		this.cause = cause;
	}
	
	public Cause getGameDataExceptionCause() {
		return cause;
	}
	
	public enum Cause {
		SQL_EXCEPTION, // 
		NOT_FOUND, //
		UNKNOWN, //
		NO_PERMISSION,
	}
}