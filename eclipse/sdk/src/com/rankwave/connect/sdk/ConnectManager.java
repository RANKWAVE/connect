package com.rankwave.connect.sdk;

import java.util.HashMap;

import android.os.Handler;
import android.util.Log;

import com.rankwave.connect.sdk.ConnectCallback.FuncResult;


public class ConnectManager {
	private static ConnectCallback<ConnectSession> initialize_connect_callback = null;
	private static ConnectCallback<ConnectSession> auto_login_connect_callback = null;
	private static ConnectCallback<ConnectSession> login_connect_callback = null;
	private static ConnectCallback<ConnectSession> join_connect_callback = null;
	private static ConnectCallback<ConnectSession> gcm_connect_callback = null;
	
	private static Thread thread = null;
	
	static HashMap<String, Object> info = new HashMap<String, Object>();
	
	public static void sdkInitialize(ConnectCallback<ConnectSession> connectCallback) {
		initialize_connect_callback = connectCallback;
		ConnectService.initialize(new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				initialize_connect_callback.onSuccess(connectSession);
				
				if(thread == null || !thread.isAlive()){
					thread = new ConnectPollingThread();
					thread.start();
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				initialize_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	public static void autoLogin(ConnectCallback<ConnectSession> connectCallback){
		auto_login_connect_callback = connectCallback;
		
		ConnectSession connectSession = Connect.getConnectSession();
		
		String savedConnectToken = connectSession.loadSavedConnectToken();
		String idType = connectSession.loadIdType();
		String snsType = connectSession.loadSnsType();
		
		if(savedConnectToken != null && !"".equals(savedConnectToken) &&
				idType != null && !"".equals(idType)){
			
			if(IdType.toEnum(idType).equals(IdType.ID_TYPE_SNS)){
				String accessToken = connectSession.loadSnsAccessToken();
				String tokenSecret = connectSession.loadSnsTokenSecret();
				
				if(SnsType.toEnum(snsType).equals(SnsType.SNS_TYPE_FACEBOOK)){
					setFacebookToken(accessToken, new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							if(auto_login_connect_callback != null)
								auto_login_connect_callback.onSuccess(connectSession);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							Connect.getConnectSession().deleteSavedConnectToken();
							if(auto_login_connect_callback != null)
								auto_login_connect_callback.onFail(result, exception);
						}
					});
				}else if(SnsType.toEnum(snsType).equals(SnsType.SNS_TYPE_TWITTER)){
					setTwitterToken(accessToken, tokenSecret, new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							if(auto_login_connect_callback != null)
								auto_login_connect_callback.onSuccess(connectSession);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							Connect.getConnectSession().deleteSavedConnectToken();
							if(auto_login_connect_callback != null)
								auto_login_connect_callback.onFail(result, exception);
						}
					});
				}
			}else if(IdType.toEnum(idType).equals(IdType.ID_TYPE_EMAIL)){
				String email = connectSession.loadId();
				
				emailLogin(email, new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							if(auto_login_connect_callback != null)
								auto_login_connect_callback.onSuccess(connectSession);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							Connect.getConnectSession().deleteSavedConnectToken();
							if(auto_login_connect_callback != null)
								auto_login_connect_callback.onFail(result, exception);
						}
					});
			}else if(IdType.toEnum(idType).equals(IdType.ID_TYPE_ANONYMOUS)){
				anonymousLogin(new ConnectCallback<ConnectSession>(){
					@Override
					public void onSuccess(ConnectSession connectSession){
						if(auto_login_connect_callback != null)
							auto_login_connect_callback.onSuccess(connectSession);
					}
					
					@Override
					public void onFail(FuncResult result, Exception exception){
						Connect.getConnectSession().deleteSavedConnectToken();
						if(auto_login_connect_callback != null)
							auto_login_connect_callback.onFail(result, exception);
					}
				});
			}
			
		}else{
			if(auto_login_connect_callback != null)
				auto_login_connect_callback.onFail(FuncResult.E_NOT_EXIST_SAVED_SESSION, new Exception("saved Session not exist."));
		}
	}
	
	
	/**
	 * setFacebookToken
	 *  
	 * @param faceook_access_token 
	 * @param connectCallback 
	 */
	public static void setFacebookToken(String faceook_access_token, ConnectCallback<ConnectSession> connectCallback) {
		login_connect_callback = connectCallback;
		
		ConnectSession connectSession = Connect.getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(faceook_access_token == null || faceook_access_token.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("faceook_access_token can not be empty."));
			}
			return;
		}
		
		info.put("facebook_access_token", faceook_access_token);
		
		ConnectService.token(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_FACEBOOK, info, new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				Boolean joined = connectSession.getUser().getJoined();
				if(joined){
					ConnectService.login(new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
														
							ConnectService.profileGet(new ConnectCallback<User>(){
								@Override
								public void onSuccess(User profileUser){
									ConnectManager.profileUserCopy(profileUser);
									
									ConnectSession connectSession = Connect.getConnectSession();
									
									connectSession.storeConnectToken(connectSession.getConnect_token());
									connectSession.setConnectSessionState(ConnectSessionState.OPENED);
									
									if(Connect.getSession_save_flag() == null || Connect.getSession_save_flag()){
										connectSession.storeSavedConnectToken(connectSession.getConnect_token());
										connectSession.storeIdType(IdType.toString(IdType.ID_TYPE_SNS));
										connectSession.storeSnsType(SnsType.toString(SnsType.SNS_TYPE_FACEBOOK));
										connectSession.storeSnsAccessToken(info.get("facebook_access_token").toString());
										connectSession.storeId(connectSession.getUser().getId());
									}
									
									login_connect_callback.onSuccess(connectSession);
								}
								
								@Override
								public void onFail(FuncResult result, Exception exception){
									login_connect_callback.onFail(result, exception);
								}
							});
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							login_connect_callback.onFail(result, exception);
						}
					});
				}else{
					if(Connect.getAuto_join_flag() == null || Connect.getAuto_join_flag()){
						
						join(null, new ConnectCallback<ConnectSession>(){
							@Override
							public void onSuccess(ConnectSession connectSession){
								
								connectSession.storeConnectToken(connectSession.getConnect_token());
								connectSession.setConnectSessionState(ConnectSessionState.OPENED);
																
								login_connect_callback.onSuccess(connectSession);
							}
							
							@Override
							public void onFail(FuncResult result, Exception exception){
								login_connect_callback.onFail(result, exception);
							}
						});
					}else{
						//SNS 정보를 조회하여 셋팅.
						ConnectService.getSnsInfo(SnsType.SNS_TYPE_FACEBOOK, info, new ConnectCallback<ConnectSession>(){
							@Override
							public void onSuccess(ConnectSession connectSession){
								connectSession.storeConnectToken(connectSession.getConnect_token());	//join 시 connect_token 이 필요하기 때문에 저장해둔다.
								login_connect_callback.onSuccess(connectSession);
							}
							
							@Override
							public void onFail(FuncResult result, Exception exception){
								login_connect_callback.onFail(result, exception);
							}
						});
					}
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	/**
	 * setTwitterToken
	 *  
	 * @param twitter_access_token
	 * @param twitter_token_secret
	 * @param connectCallback
	 * @param autoJoinFlag 
	 */
	public static void setTwitterToken(String twitter_access_token, String twitter_token_secret, ConnectCallback<ConnectSession> connectCallback) {
		login_connect_callback = connectCallback;
		
		ConnectSession connectSession = Connect.getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(twitter_access_token == null || twitter_access_token.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("twitter_access_token can not be empty."));
			}
			return;
		}
		
		if(twitter_token_secret == null || twitter_token_secret.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("twitter_token_secret can not be empty."));
			}
			return;
		}
		
		login_connect_callback = connectCallback;
		
		info.put("twitter_access_token", twitter_access_token);
		info.put("twitter_token_secret", twitter_token_secret);
		
		ConnectService.token(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_TWITTER, info, new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				Boolean join = connectSession.getUser().getJoined();
				if(join){
					ConnectService.login(new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							ConnectService.profileGet(new ConnectCallback<User>(){
								@Override
								public void onSuccess(User profileUser){
									ConnectManager.profileUserCopy(profileUser);
									
									ConnectSession connectSession = Connect.getConnectSession();
									
									connectSession.storeConnectToken(connectSession.getConnect_token());
									connectSession.setConnectSessionState(ConnectSessionState.OPENED);
									
									if(Connect.getSession_save_flag() == null || Connect.getSession_save_flag()){
										connectSession.storeSavedConnectToken(connectSession.getConnect_token());
										connectSession.storeIdType(IdType.toString(IdType.ID_TYPE_SNS));
										connectSession.storeSnsType(SnsType.toString(SnsType.SNS_TYPE_TWITTER));
										connectSession.storeSnsAccessToken(info.get("twitter_access_token").toString());
										connectSession.storeSnsTokenSecret(info.get("twitter_token_secret").toString());
										connectSession.storeId(connectSession.getUser().getId());
									}
																		
									login_connect_callback.onSuccess(connectSession);
								}
								
								@Override
								public void onFail(FuncResult result, Exception exception){
									login_connect_callback.onFail(result, exception);
								}
							});
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							login_connect_callback.onFail(result, exception);
						}
					});
				}else{
					if(Connect.getAuto_join_flag() == null || Connect.getAuto_join_flag()){
						join(null, new ConnectCallback<ConnectSession>(){
							@Override
							public void onSuccess(ConnectSession connectSession){

								connectSession.storeConnectToken(connectSession.getConnect_token());
								connectSession.setConnectSessionState(ConnectSessionState.OPENED);
								
								if(Connect.getSession_save_flag() == null || Connect.getSession_save_flag()){
									connectSession.storeSavedConnectToken(connectSession.getConnect_token());
									connectSession.storeIdType(IdType.toString(IdType.ID_TYPE_SNS));
									connectSession.storeSnsType(SnsType.toString(SnsType.SNS_TYPE_TWITTER));
									connectSession.storeSnsAccessToken(info.get("twitter_access_token").toString());
									connectSession.storeSnsTokenSecret(info.get("twitter_token_secret").toString());
									connectSession.storeId(connectSession.getUser().getId());
								}
								
								login_connect_callback.onSuccess(connectSession);
							}
							
							@Override
							public void onFail(FuncResult result, Exception exception){
								login_connect_callback.onFail(result, exception);
							}
						});
					}else{
						//SNS 정보를 조회하여 셋팅.
						ConnectService.getSnsInfo(SnsType.SNS_TYPE_TWITTER, info, new ConnectCallback<ConnectSession>(){
							@Override
							public void onSuccess(ConnectSession connectSession){
								connectSession.storeConnectToken(connectSession.getConnect_token());	//join 시 connect_token 이 필요하기 때문에 저장해둔다.
								login_connect_callback.onSuccess(connectSession);
							}
							
							@Override
							public void onFail(FuncResult result, Exception exception){
								login_connect_callback.onFail(result, exception);
							}
						});
					}
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	/**
	 * emailLogin
	 * @param email
	 * @param connectCallback
	 */
	public static void emailLogin(String email, ConnectCallback<ConnectSession> connectCallback) {
		login_connect_callback = connectCallback;
		
		ConnectSession connectSession = Connect.getConnectSession();
		if(connectSession == null){
			Log.e(Connect.TAG, "ConnectSession is null : SDK is not initialized.");
			
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null : SDK is not initialized."));
			}
			return;
		}
		
		if(email == null || email.equals("")){
			if(connectCallback != null){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("email can not be empty."));
			}
			return;
		}
		
		
		login_connect_callback = connectCallback;
		
		info.put("email", email);
		
		ConnectService.token(IdType.ID_TYPE_EMAIL, null, info, new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				Boolean join = connectSession.getUser().getJoined();
				if(join){
					ConnectService.login(new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							ConnectService.profileGet(new ConnectCallback<User>(){
								@Override
								public void onSuccess(User profileUser){
									ConnectManager.profileUserCopy(profileUser);
									
									ConnectSession connectSession = Connect.getConnectSession();
									
									connectSession.storeConnectToken(connectSession.getConnect_token());
									connectSession.setConnectSessionState(ConnectSessionState.OPENED);
									
									if(Connect.getSession_save_flag() == null || Connect.getSession_save_flag()){
										connectSession.storeSavedConnectToken(connectSession.getConnect_token());
										connectSession.storeIdType(IdType.toString(IdType.ID_TYPE_EMAIL));
										connectSession.storeId(info.get("email").toString());
									}
																		
									login_connect_callback.onSuccess(connectSession);
								}
								
								@Override
								public void onFail(FuncResult result, Exception exception){
									login_connect_callback.onFail(result, exception);
								}
							});
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							login_connect_callback.onFail(result, exception);
						}
					});
				}else{
					if(Connect.getAuto_join_flag() == null || Connect.getAuto_join_flag()){
						join(null, new ConnectCallback<ConnectSession>(){
							@Override
							public void onSuccess(ConnectSession connectSession){

								connectSession.storeConnectToken(connectSession.getConnect_token());
								connectSession.setConnectSessionState(ConnectSessionState.OPENED);
								
								if(Connect.getSession_save_flag() == null || Connect.getSession_save_flag()){
									connectSession.storeSavedConnectToken(connectSession.getConnect_token());
									connectSession.storeIdType(IdType.toString(IdType.ID_TYPE_EMAIL));
									connectSession.storeId(info.get("email").toString());
								}
								
								login_connect_callback.onSuccess(connectSession);
							}
							
							@Override
							public void onFail(FuncResult result, Exception exception){
								login_connect_callback.onFail(result, exception);
							}
						});
					}
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	public static void anonymousLogin(ConnectCallback<ConnectSession> connectCallback) {
		login_connect_callback = connectCallback;
		
		ConnectService.token(IdType.ID_TYPE_ANONYMOUS, null, null, new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				Boolean join = connectSession.getUser().getJoined();
				if(join){
					ConnectService.login(new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							connectSession.setConnectSessionState(ConnectSessionState.OPENED);
							
							if(Connect.getSession_save_flag() == null || Connect.getSession_save_flag()){
								connectSession.storeSavedConnectToken(connectSession.getConnect_token());
								connectSession.storeIdType(IdType.toString(IdType.ID_TYPE_ANONYMOUS));
							}
							
							login_connect_callback.onSuccess(connectSession);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							login_connect_callback.onFail(result, exception);
						}
					});
				}else{
					join(null, new ConnectCallback<ConnectSession>(){
						@Override
						public void onSuccess(ConnectSession connectSession){
							connectSession.setConnectSessionState(ConnectSessionState.OPENED);
							
							if(Connect.getSession_save_flag() == null || Connect.getSession_save_flag()){
								connectSession.storeSavedConnectToken(connectSession.getConnect_token());
								connectSession.storeIdType(IdType.toString(IdType.ID_TYPE_ANONYMOUS));
							}
							
							login_connect_callback.onSuccess(connectSession);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							login_connect_callback.onFail(result, exception);
						}
					});
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	public static void join(Profile profile, ConnectCallback<ConnectSession> connectCallback){
		join_connect_callback = connectCallback;
		
		ConnectService.join(profile, new ConnectCallback<ConnectSession>(){
			@Override
			public void onSuccess(ConnectSession connectSession){
				ConnectService.login(new ConnectCallback<ConnectSession>(){
					@Override
					public void onSuccess(ConnectSession connectSession){
						ConnectService.profileGet(new ConnectCallback<User>(){
							@Override
							public void onSuccess(User profileUser){
								ConnectManager.profileUserCopy(profileUser);
								
								ConnectSession connectSession = Connect.getConnectSession();
								
								connectSession.storeConnectToken(connectSession.getConnect_token());
								connectSession.setConnectSessionState(ConnectSessionState.OPENED);
								
								if(Connect.getSession_save_flag() == null || Connect.getSession_save_flag()){
									connectSession.storeSavedConnectToken(connectSession.getConnect_token());
									connectSession.storeIdType(IdType.toString(connectSession.getUser().getIdType()));
									
									if(connectSession.getUser().getIdType() == IdType.ID_TYPE_SNS){
										connectSession.storeSnsType(SnsType.toString(connectSession.getUser().getSnsType()));
										connectSession.storeSnsAccessToken(connectSession.getUser().getSnsInfo().getAccessToken());
										connectSession.storeSnsTokenSecret(connectSession.getUser().getSnsInfo().getTokenSecret());
										connectSession.storeId(connectSession.getUser().getId());
									}else if(connectSession.getUser().getIdType() == IdType.ID_TYPE_EMAIL){
										connectSession.storeId(connectSession.getUser().getId());
									}
								}
								
								join_connect_callback.onSuccess(connectSession);
							}
							
							@Override
							public void onFail(FuncResult result, Exception exception){
								join_connect_callback.onFail(result, exception);
							}
						});
					}
					
					@Override
					public void onFail(FuncResult result, Exception exception){
						join_connect_callback.onFail(result, exception);
					}
				});
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				join_connect_callback.onFail(result, exception);
			}
			
		});
	}
		
	
	public static void logout(ConnectCallback<ConnectSession> connectCallback){
		ConnectService.logout(connectCallback);
	}
	
	public static void leave(ConnectCallback<ConnectSession> connectCallback){
		ConnectService.leave(connectCallback);
	}
	
	public static void profileUpdate(Profile profile, ConnectCallback<ConnectSession> connectCallback){
		ConnectService.profileUpdate(profile, connectCallback);
	}
	
	public static void setGCMRegistrationId(ConnectCallback<ConnectSession> connectCallback){
		gcm_connect_callback = connectCallback;
		
		String regid = GCMManager.getInstance().getRegistrationId(Connect.getContext());
		if (regid == null || regid.length() == 0) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					
					setGCMRegistrationId(gcm_connect_callback);
					
				}
			}, 1000);
		}else{
			ConnectService.setGCMRegistrationId(connectCallback);
		}
	}
	
	
	public static void unsetGCMRegistrationId(ConnectCallback<ConnectSession> connectCallback){
		ConnectService.unsetGCMRegistrationId(connectCallback);
	}
	
	public static void pushOn(ConnectCallback<ConnectSession> connectCallback){
		gcm_connect_callback = connectCallback;
		
		String regid = GCMManager.getInstance().getRegistrationId(Connect.getContext());
		if (regid == null || regid.length() == 0) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					
					pushOn(gcm_connect_callback);
					
				}
			}, 1000);
		}else{
			ConnectService.pushOn(connectCallback);
		}
	}
	
	public static void pushOff(ConnectCallback<ConnectSession> connectCallback){
		ConnectService.pushOff(connectCallback);
	}
	
	private static void profileUserCopy(User profileUser){
		ConnectSession connectSession = Connect.getConnectSession();
		connectSession.getUser().setProfile(profileUser.getProfile());
		connectSession.getUser().setSnsInfo(profileUser.getSnsInfo());
		
		connectSession.getUser().setId(profileUser.getId());
		connectSession.getUser().setIdType(profileUser.getIdType());
		connectSession.getUser().setSnsType(profileUser.getSnsType());
		connectSession.getUser().setEmailVerify(profileUser.getEmailVerify());
		connectSession.getUser().setJoined(profileUser.getJoined());

	}
}
