package com.rankwave.sdkdemo;

import java.io.Serializable;

public class SnsUser implements Serializable{
	private String name;
	private String sns_id;
	private String sns_type;
	private String id_type;
	private String access_token;
	private String token_secret;
	private String profile_image;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSns_id() {
		return sns_id;
	}
	public void setSns_id(String sns_id) {
		this.sns_id = sns_id;
	}
	public String getSns_type() {
		return sns_type;
	}
	public void setSns_type(String sns_type) {
		this.sns_type = sns_type;
	}
	public String getId_type() {
		return id_type;
	}
	public void setId_type(String id_type) {
		this.id_type = id_type;
	}
	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public String getToken_secret() {
		return token_secret;
	}
	public void setToken_secret(String token_secret) {
		this.token_secret = token_secret;
	}
	public String getProfile_image() {
		return profile_image;
	}
	public void setProfile_image(String profile_image) {
		this.profile_image = profile_image;
	}
	
}
