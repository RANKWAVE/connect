package com.rankwave.connect.sdk;

public abstract class ConnectCallback<T> {
		
	abstract public void onSuccess(T resultObj);
	
	abstract public void onFail(FuncResult result, Exception exception);
	
	
	public enum FuncResult {
		E_SUCCEED,
		E_FAIL,
		E_INVALID_SNS_TOKEN,
		E_NOT_JOINED,
		E_NOT_EXIST_SAVED_SESSION
	}
}

