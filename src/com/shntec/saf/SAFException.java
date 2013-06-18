package com.shntec.saf;

public class SAFException extends Exception {

	private int ErrorCode;
	private String ErrorMsg;
	
	public SAFException(int code, String msg){
		super("ErrorCode: "+code+" "+msg, new Throwable());
	}
	public SAFException(int code, String msg, Throwable throwable){
		super("ErrorCode: "+code+" "+msg, throwable);
	}


	public int getErrorCode() {
		return ErrorCode;
	}
	public void setErrorCode(int errorCode) {
		ErrorCode = errorCode;
	}
	public String getErrorMsg() {
		return ErrorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		ErrorMsg = errorMsg;
	}
	
	
}
