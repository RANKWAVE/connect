package com.rankwave.connect.sdk;

public class SnsInfo {
	private String snsId;
	private SnsType snsType;
	private String accessToken;
	private String tokenSecret;
	private String profileUrl;
			
	public SnsInfo() {

	}

	public String getSnsId() {
		return snsId;
	}

	public void setSnsId(String snsId) {
		this.snsId = snsId;
	}

	public SnsType getSnsType() {
		return snsType;
	}

	public void setSnsType(SnsType snsType) {
		this.snsType = snsType;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	

	@Override
	public String toString() {
		return "SnsInfo [snsId=" + snsId + ", snsType=" + snsType
				+ ", accessToken=" + accessToken + ", tokenSecret="
				+ tokenSecret + ", profileUrl=" + profileUrl + "]";
	}
	
	public void clearInfo() {
		snsId = "";
		accessToken = "";
		tokenSecret = "";
		profileUrl = "";
	}
}
