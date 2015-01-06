package com.rankwave.connect.sdk;

public interface ConnectCallback<T> {
		
	public void onSuccess(T resultObj);
	
	public void onFail(FuncResult result, Exception exception);
	
	public enum FuncResult {
		
		E_SUCCEED,
		E_FAIL,
		E_ALREADY_REGISTED_EMAIL,
		E_INVALID_EMAIL,
		E_INVALID_PASSWORD,
	}
}

