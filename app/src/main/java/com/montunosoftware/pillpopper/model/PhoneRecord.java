package com.montunosoftware.pillpopper.model;

public class PhoneRecord
{
	private String _phoneNumber;
	private String _type;
	
	public PhoneRecord(String phoneNumber, String type) {
		this._phoneNumber = phoneNumber;
		this._type = type;
	}
	
	public String getPhoneNumber() { return _phoneNumber; }
	public String getType() { return _type; }
}
