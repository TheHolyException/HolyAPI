package de.theholyexception.holyapi.datastorage.dataconnection.models;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import de.theholyexception.holyapi.datastorage.dataconnection.enums.DataDefaultValue;
import de.theholyexception.holyapi.datastorage.dataconnection.enums.DataType;
import de.theholyexception.holyapi.datastorage.dataconnection.enums.IndexType;
import de.theholyexception.holyapi.datastorage.dataconnection.enums.TableRowFormat;

public class TableModel {
	
	private TableRowFormat rowFormat = TableRowFormat.DEFAULT;
	private List<ColumnModel> columns = new ArrayList<>();
	private List<ColumnModel> indices = new ArrayList<>();
	private String collation = "utf8mb3_general_ci";
	
	public TableModel(ResultSet resultSet) {
		try {
			ResultSetMetaData metaData = resultSet.getMetaData();
			
			for (int i = 0; i < metaData.getColumnCount(); i ++) {
				ColumnModel model = new ColumnModel(metaData.getColumnName(i), DataType.parseString(metaData.getColumnTypeName(i)))
						.setAllowNull(metaData.isNullable(i) == 1)
						.setUnsigned(!metaData.isSigned(i));
				
				if (metaData.isAutoIncrement(i)) model.setDefaultValueMode(DataDefaultValue.AUTO_INCREMENT);
				
				columns.add(model);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public TableModel(List<ColumnModel> columns) {
		this.columns = columns;
	}
	
	public TableModel setCollation(String collation) {
		this.collation = collation;
		return this;
	}
	
	public TableModel setRowFormat(TableRowFormat rowFormat) {
		this.rowFormat = rowFormat;
		return this;
	}
	
	public TableModel addColumn(ColumnModel column) {
		columns.add(column);
		if (column.indexType != null) indices.add(column);
		return this;
	}
	
	public TableModel addColumn(int slot, ColumnModel column) {
		columns.add(slot, column);
		if (column.indexType != null) indices.add(column);
		return this;
	}
	
	public TableModel setColumn(int slot, ColumnModel column) {
		if (columns.size() < slot) throw new IndexOutOfBoundsException("Table only have " + columns.size() + " slots < " + slot);
		columns.add(slot, column);
		if (column.indexType != null) indices.add(column);
		return this;
	}
	
	/*
	CREATE TABLE `tbltest` (
	`asdf` INT NOT NULL AUTO_INCREMENT,
	`asdf2` INT(2) NOT NULL,
	`asdf3` INT NOT NULL,
	`asdf4` INT NOT NULL,
	`asdf5` INT NOT NULL,
	`asdf6` INT NOT NULL,
	`asdf7` DECIMAL(20,6) NOT NULL DEFAULT 0,
	`asdf8` MULTIPOINT NOT NULL,
	`asdf9` MULTIPOINT NULL DEFAULT NULL,
	`asdf10` BIGINT NOT NULL,
	`Spalte 11` BIGINT ZEROFILL NOT NULL DEFAULT '0',
	PRIMARY KEY (`asdf`),
	INDEX `asdf2` (`asdf2`),
	UNIQUE INDEX `asdf3` (`asdf3`)
	)
	COLLATE='utf8mb3_general_ci'
	;

	 */
	
	public boolean checkValid() {
		return true;
	}
	
	public String toNewTable(String name) {
		if (!checkValid()) return null;
		StringBuilder builder = new StringBuilder();
		builder
		.append("CREATE TABLE `")
		.append(name)
		.append("` (");
		
		for (ColumnModel column : columns) {
			builder.append("`").append(column.getName()).append("`")
			.append(column.getType().toString());
			if (column.getSize() != null)
				builder.append("(").append(column.getSize()).append(")");
			if (column.isUnsigned()) builder.append(" UNSIGNED");
			if (column.isZeroFill()) builder.append(" ZEROFILL");
			builder.append((column.isAllowNull() ? " NULL" : " NOT NULL"));
			if (column.defaultValueMode.equals(DataDefaultValue.CUSTOM)) {
				builder.append(" DEFAULT '").append(column.defaultValue).append('\'');
			} else {
				builder.append(" ").append(column.defaultValueMode);
			}
			builder.append(',');
		}
		
		for (ColumnModel column : indices) {
			builder.append(column.getIndexType().toString() + (column.getIndexType().equals(IndexType.PRIMARY) ? " KEY" : ""));
			if (!column.indexType.equals(IndexType.PRIMARY)) {
				builder.append("`").append(column.getName()).append("`");
			}
			builder.append(" (`").append(column.getName()).append("`)");
			if (!column.equals(indices.get(indices.size()-1))) builder.append(',');
		}
		
		builder.append(") COLLATE='").append(collation);
		if (!rowFormat.equals(TableRowFormat.DEFAULT)) {
			builder.append(" ROW_FORMAT=").append(rowFormat.toString());
		}
		builder.append("';");
		return builder.toString();
	}

}
