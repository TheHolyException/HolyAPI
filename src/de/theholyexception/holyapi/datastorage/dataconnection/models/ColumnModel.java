package de.theholyexception.holyapi.datastorage.dataconnection.models;

import java.util.Properties;

import de.theholyexception.holyapi.datastorage.dataconnection.enums.DataDefaultValue;
import de.theholyexception.holyapi.datastorage.dataconnection.enums.DataType;
import de.theholyexception.holyapi.datastorage.dataconnection.enums.IndexType;

public class ColumnModel {

	private final String name;
	private final DataType type;
	private Properties typeData;
	private Object size;
	boolean unsigned;
	boolean allowNull;
	boolean zeroFill;
	DataDefaultValue defaultValueMode;
	Object defaultValue;
	IndexType indexType;
	
	public ColumnModel(String name, DataType type) {
		this.name = name;
		this.type = type;
	}
	
	public ColumnModel setTypeData(Properties typeData) {
		this.typeData = typeData;
		return this;
	}
	
	public ColumnModel setSize(Object size) {
		this.size = size;
		return this;
	}
	
	public ColumnModel setUnsigned(boolean unsigned) {
		this.unsigned = unsigned;
		return this;
	}
	
	public ColumnModel setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
		return this;
	}
	
	public ColumnModel setZeroFill(boolean zeroFill) {
		this.zeroFill = zeroFill;
		return this;
	}
	
	public ColumnModel setDefaultValueMode(DataDefaultValue defaultValueMode) {
		this.defaultValueMode = (defaultValueMode == null ? DataDefaultValue.NULL : defaultValueMode);
		return this;
	}
	
	public ColumnModel setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public ColumnModel setIndexType(IndexType indexType) {
		this.indexType = indexType;
		return this;
	}
	
	
	

	public String getName() {
		return name;
	}

	public DataType getType() {
		return type;
	}

	public Properties getTypeData() {
		return typeData;
	}

	public Object getSize() {
		return size;
	}

	public boolean isUnsigned() {
		return unsigned;
	}
	
	public boolean isAllowNull() {
		return allowNull;
	}

	public boolean isZeroFill() {
		return zeroFill;
	}

	public DataDefaultValue getDefaultValueMode() {
		return defaultValueMode;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public IndexType getIndexType() {
		return indexType;
	}
	
}
