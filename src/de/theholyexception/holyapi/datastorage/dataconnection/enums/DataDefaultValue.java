package de.theholyexception.holyapi.datastorage.dataconnection.enums;

public enum DataDefaultValue {

	NULL("DEFAULT NULL"),
	AUTO_INCREMENT("AUTO_INCREMENT"),
	CUSTOM(null);
	
	private String value;
	
	DataDefaultValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
}
