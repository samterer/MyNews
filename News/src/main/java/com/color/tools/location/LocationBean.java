package com.color.tools.location;

import java.io.Serializable;

public class LocationBean implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String province;
	private String city;
	private String town;
	private String street;
	private String streetNumber;
	private String cityCode;
	private String details;
	
	public LocationBean(){
		
	}
	
	public LocationBean(String province, String city, String town,
			String street, String streetNumber, String cityCode, String details) {
		super();
		province=province.replace("省", "");
		city=city.replace("市", "");
		city=city.replace("县", "");
		town=town.replace("县", "");
		town=town.replace("区", "");
		
		this.province = province;
		this.city = city;
		this.town = town;
		this.street = street;
		this.streetNumber = streetNumber;
		this.cityCode = cityCode;
		this.details = details;
	}
	
	
	public String getProvince() {
		return province;
	}
	public String getCity() {
		return city;
	}
	public String getTown() {
		return town;
	}
	public String getStreet() {
		return street;
	}
	public String getStreetNumber() {
		return streetNumber;
	}
	public String getCityCode() {
		return cityCode;
	}
	public String getDetails() {
		return details;
	}
	public void setProvince(String province) {
		province=province.replace("省", "");
		this.province = province;
	}
	public void setCity(String city) {
		city=city.replace("市", "");
		city=city.replace("县", "");
		this.city = city;
	}
	public void setTown(String town) {
		town=town.replace("县", "");
		town=town.replace("区", "");
		this.town = town;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public void setStreetNumber(String streetNumber) {
		this.streetNumber = streetNumber;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	
	
	
	
	
}
