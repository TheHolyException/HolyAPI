package de.theholyexception.holyapi.datastorage.dataconnection.enums;

public enum DataType {

	//Ganzzahlig
	TINYINT(1),
	SMALLINT(1),
	MEDIUMINT(1),
	INT(1),
	BIGINT(1),
	BIT(1),
	//Fließkomma
	FLOAT(1),
	DOUBLE(1),
	DECIMAL(2),
	//TEXT
	VARCHAR(1),
	CHAR(1),
	TINYTEXT(1),
	TEXT(1),
	MEDIUMTEXT(1),
	LONGTEXT(1),
	JSON(1),
	//Binär
	BINARY(1),
	VARBINARY(1),
	TINYBLOB(1),
	BLOB(1),
	MEDIUMBLOB(1),
	LONGBLOB(1),
	//Zeitlich
	DATE(0),
	TIME(0),
	YEAR(0),
	DATETIME(0),
	TIMESTAMP(0),
	//Geometrisch
	POINT(0),
	LINESTRING(0),
	POLYGON(0),
	GEOMETRY(0),
	MULTIPOINT(0),
	MULTILINESTRING(0),
	MULTIPOLYGON(0),
	GEOMETRYCOLLECTION(0);
	
	boolean allowLength;
	boolean allowSet;
	
	DataType(int mode) {
		allowLength = ((mode >>> 0) & 1) != 0;
		allowSet 	= ((mode >>> 1) & 1) != 0;
	}
	
	public boolean isAllowLength() {
		return allowLength;
	}
	
	public boolean isAllowSet() {
		return allowSet;
	}
	
	public static DataType parseString(String s) {
		for (DataType type : DataType.values()) {
			if (type.toString().toLowerCase().equals(s.toLowerCase())) return type;
		}
		return null;
	}
	
}
