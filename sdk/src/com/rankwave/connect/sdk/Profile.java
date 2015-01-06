package com.rankwave.connect.sdk;

public class Profile {
	private String email;
	private String name;
	private String birthday;
	private String gender;
	private Residence residence = new Residence();
	private Hometown hometown = new Hometown();
	private String timezone;
	private String locale;
	
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Residence getResidence() {
		return residence;
	}

	public void setResidence(Residence residence) {
		this.residence = residence;
	}

	public Hometown getHometown() {
		return hometown;
	}

	public void setHometown(Hometown hometown) {
		this.hometown = hometown;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	

	@Override
	public String toString() {
		return "Profile [email=" + email + ", name=" + name + ", birthday="
				+ birthday + ", gender=" + gender + ", residence=" + residence
				+ ", hometown=" + hometown + ", timezone=" + timezone
				+ ", locale=" + locale + "]";
	}



	public class Residence{
		public Residence(){
			
		}
		private String country;
		private String states;
		private String city;
		private String street;
		private String spot;
		public String getCountry() {
			return country;
		}
		public void setCountry(String country) {
			this.country = country;
		}
		public String getStates() {
			return states;
		}
		public void setStates(String states) {
			this.states = states;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getStreet() {
			return street;
		}
		public void setStreet(String street) {
			this.street = street;
		}
		public String getSpot() {
			return spot;
		}
		public void setSpot(String spot) {
			this.spot = spot;
		}
		@Override
		public String toString() {
			return "Residence [country=" + country + ", states=" + states
					+ ", city=" + city + ", street=" + street + ", spot="
					+ spot + "]";
		}
		
	}
	
	
	public class Hometown{
		public Hometown(){
			
		}
		private String country;
		private String states;
		private String city;
		private String street;
		private String spot;
		public String getCountry() {
			return country;
		}
		public void setCountry(String country) {
			this.country = country;
		}
		public String getStates() {
			return states;
		}
		public void setStates(String states) {
			this.states = states;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getStreet() {
			return street;
		}
		public void setStreet(String street) {
			this.street = street;
		}
		public String getSpot() {
			return spot;
		}
		public void setSpot(String spot) {
			this.spot = spot;
		}
		@Override
		public String toString() {
			return "Hometown [country=" + country + ", states=" + states
					+ ", city=" + city + ", street=" + street + ", spot="
					+ spot + "]";
		}
		
	}
}
