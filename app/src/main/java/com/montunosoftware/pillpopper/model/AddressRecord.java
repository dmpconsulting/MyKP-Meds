package com.montunosoftware.pillpopper.model;

public class AddressRecord
{
	private String _address;
	private String _type;
	
	public AddressRecord(String address, String type) {
		this._address = address;
		this._type = type;
	}
	
	public String getAddress() { return _address; }
	public String getType() { return _type; }
}
