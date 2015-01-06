package com.rankwave.connect.sdk;


public class User {
	private IdType idType;
	private SnsType snsType;
	private SnsInfo snsInfo;
	
	private Boolean emailVerify;
	private Boolean joined;

		
	public User() {
		snsInfo = new SnsInfo();
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
		private String name;
		private String birthday;
		
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

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getBirthday() {
			return birthday;
		}

		public void setBirthday(String birthday) {
			this.birthday = birthday;
		}

		@Override
		public String toString() {
			return "SnsInfo [snsId=" + snsId + ", snsType=" + snsType
					+ ", accessToken=" + accessToken + ", tokenSecret="
					+ tokenSecret + ", profileUrl=" + profileUrl + ", name=" + name
					+ ", birthday=" + birthday + "]";
		}
		
		
		public void clearInfo() {
			snsId = "";
			accessToken = "";
			tokenSecret = "";
			profileUrl = "";
			name = "";
			birthday = "";
		}
	}



	@Override
	public String toString() {
		return "User [idType=" + idType + ", snsType=" + snsType + ", snsInfo="
				+ snsInfo + ", emailVerify=" + emailVerify + ", joined="
				+ joined + "]";
	}
	
	
}
