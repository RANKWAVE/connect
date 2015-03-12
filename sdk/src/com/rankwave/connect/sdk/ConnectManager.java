package com.rankwave.connect.sdk;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.rankwave.connect.sdk.ConnectCallback.FuncResult;


public class ConnectManager {
	private static ConnectCallback<ConnectSession> initialize_connect_callback = null;
	private static ConnectCallback<ConnectSession> login_connect_callback = null;
	private static ConnectCallback<ConnectSession> join_connect_callback = null;
	private static ConnectCallback<ConnectSession> gcm_connect_callback = null;
		
	public static void sdkInitialize(ConnectCallback<ConnectSession> connectCallback) {
		initialize_connect_callback = connectCallback;
		ConnectService.initialize(new ConnectCallback<ConnectSession>(){
			@Override
        	public void onSuccess(ConnectSession connectSession){
				//session clear
				connectSession.connectSessionClear();
				
				//saved session process
				String savedConnectToken = connectSession.loadSavedConnectToken();
				
				Log.d(Connect.TAG, "savedConnectToken :: " + savedConnectToken);
				
				if(savedConnectToken != null && !"".equals(savedConnectToken)){
					ConnectService.profileGet(new ConnectCallback<User>(){
						@Override
						public void onSuccess(User profileUser){
							ConnectManager.profileUserCopy(profileUser);
							
							ConnectSession connectSession = Connect.getConnectSession();
							connectSession.setConnectSessionState(ConnectSessionState.OPENED);
							
							initialize_connect_callback.onSuccess(connectSession);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							initialize_connect_callback.onFail(result, exception);
						}
					});
				}else{
					initialize_connect_callback.onSuccess(connectSession);
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				initialize_connect_callback.onFail(result, exception);
			}
		});
		
		
		//push click action upload
		final SharedPreferences prefs = Connect.getContext().getSharedPreferences(Connect.SDK_PREFERENCES, Context.MODE_PRIVATE);
		String push_seq = prefs.getString(Connect.INTENT_PUSH_SEQ, "");
		
		if(!push_seq.equals("")){
			try{
				JSONObject etc = new JSONObject();
				etc.put("push_seq", push_seq);
				etc.put("os_type", DeviceInfo.getInstance().getOs_type());
				
				ConnectService.action(null, "PUSH", 1, "CONNECT SDK", etc);
				
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Connect.INTENT_PUSH_SEQ, "");
				editor.commit();
				
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
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
							login_connect_callback.onSuccess(connectSession);
						}
						
						@Override
						public void onFail(FuncResult result, Exception exception){
							login_connect_callback.onFail(result, exception);
						}
					});
				}else{
					join(null, login_connect_callback);
				}
			}
			
			@Override
			public void onFail(FuncResult result, Exception exception){
				login_connect_callback.onFail(result, exception);
			}
		});
	}
	
	
	
	/**
	 * setFacebookToken
	 *  
	 * @param faceook_access_token 
	 * @param connectCallback 
	 * @param autoJoinFlag
	 */
	public static void setFacebookToken(String faceook_access_token, ConnectCallback<ConnectSession> connectCallback) {
		login_connect_callback = connectCallback;
		
		ConnectSession connectSession = Connect.getConnectSession();
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.READY && connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		if(Connect.getFacebook_appid() == null || Connect.getFacebook_appid().equals("")){
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("Facebook App Id is null"));
			return;
		}
		
		
		HashMap<String, Object> sns_info = new HashMap<String, Object>();
		sns_info.put("facebook_access_token", faceook_access_token);
		
		ConnectService.token(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_FACEBOOK, sns_info, new ConnectCallback<ConnectSession>(){
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
								
								ConnectService.profileGet(new ConnectCallback<User>(){
									@Override
									public void onSuccess(User profileUser){
										ConnectManager.profileUserCopy(profileUser);
										
										ConnectSession connectSession = Connect.getConnectSession();
																				
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
						login_connect_callback.onSuccess(connectSession);
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
		if(connectSession != null){
			if(connectSession.getConnectSessionState() != ConnectSessionState.READY && connectSession.getConnectSessionState() != ConnectSessionState.OPENED){
				connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession's sessionState not Ready or not Open"));
				return;
			}
		}else{
			connectCallback.onFail(FuncResult.E_FAIL, new Exception("ConnectSession is null"));
			return;
		}
		
		
		login_connect_callback = connectCallback;
		
		HashMap<String, Object> sns_info = new HashMap<String, Object>();
		sns_info.put("twitter_access_token", twitter_access_token);
		sns_info.put("twitter_token_secret", twitter_token_secret);
		
		ConnectService.token(IdType.ID_TYPE_SNS, SnsType.SNS_TYPE_TWITTER, sns_info, new ConnectCallback<ConnectSession>(){
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
								ConnectService.profileGet(new ConnectCallback<User>(){
									@Override
									public void onSuccess(User profileUser){
										ConnectManager.profileUserCopy(profileUser);
										
										ConnectSession connectSession = Connect.getConnectSession();
										
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
						login_connect_callback.onSuccess(connectSession);
					}
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
	
	
	public static void connectLogin(ConnectCallback<ConnectSession> connectCallback){
		ConnectService.login(connectCallback);
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
					
					ConnectService.setGCMRegistrationId(gcm_connect_callback);
					
				}
			}, 1000);
		}else{
			ConnectService.setGCMRegistrationId(connectCallback);
		}
	}
	
	
	public static void unsetGCMRegistrationId(ConnectCallback<ConnectSession> connectCallback){
		ConnectService.unsetGCMRegistrationId(connectCallback);
	}
	

	private static void profileUserCopy(User profileUser){
		ConnectSession connectSession = Connect.getConnectSession();
		connectSession.getUser().setProfile(profileUser.getProfile());
		connectSession.getUser().setId(profileUser.getId());
		connectSession.getUser().setIdType(profileUser.getIdType());
		connectSession.getUser().setSnsType(profileUser.getSnsType());
		connectSession.getUser().setEmailVerify(profileUser.getEmailVerify());
		connectSession.getUser().setJoined(profileUser.getJoined());

	}
}
