package de.theholyexception.holyapi.datastorage.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Row {
    private final Object[] data;
    private final String[] columns;

    public Row(ResultSet result, String[] columns) throws SQLException {
        this.columns = columns;
        data = new Object[columns.length];
        for (int i = 0; i < columns.length; i ++) {
            data[i] = result.getObject(i+1);
        }
    }

    public Object get(int index) {
        return data[index];
    }

    public <T> T get(int index, Class<T> clazz) {
        Object o = data[index];
        if(o instanceof Long && clazz == Integer.class) o = (int) ((long) o);
        return clazz.cast(o);
    }

    public <T> T get(String columnName, Class<T> clazz) {
        for (int i = 0; i < columns.length; i ++) {
            if (columns[i].equals(columnName))
                return get(i, clazz);
        }
        return null;
    }

}
