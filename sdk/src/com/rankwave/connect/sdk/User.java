package com.rankwave.connect.sdk;


public class User {
	private String id;
	private IdType idType;
	private SnsType snsType;
	private SnsInfo snsInfo;
	private Profile profile;
	
	private Boolean emailVerify;
	private Boolean joined;

		
	public User() {
		snsInfo = new SnsInfo();
		profile = new Profile();
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IdType getIdType() {
		return idType;
	}

	public void setIdType(IdType idType) {
		this.idType = idType;
	}

	public SnsType getSnsType() {
		return snsType;
	}

	public void setSnsType(SnsType snsType) {
		this.snsType = snsType;
	}

	public SnsInfo getSnsInfo() {
		return snsInfo;
	}

	public void setSnsInfo(SnsInfo snsInfo) {
		this.snsInfo = snsInfo;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public Boolean getEmailVerify() {
		return emailVerify;
	}

	public void setEmailVerify(Boolean emailVerify) {
		this.emailVerify = emailVerify;
	}

	public Boolean getJoined() {
		return joined;
	}

	public void setJoined(Boolean joined) {
		this.joined = joined;
	}
	
	
	
	public class SnsInfo{
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



	@Override
	public String toString() {
		return "User [id=" + id + ", idType=" + idType + ", snsType=" + snsType
				+ ", snsInfo=" + snsInfo + ", profile=" + profile
				+ ", emailVerify=" + emailVerify + ", joined=" + joined + "]";
	}


	
}
