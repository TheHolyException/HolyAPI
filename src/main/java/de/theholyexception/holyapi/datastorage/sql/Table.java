package de.theholyexception.holyapi.datastorage.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Table {

    private final List<Row> rows = new ArrayList<>();
    private String[] columns;

    public Table(ResultSet result) throws SQLException {
        ResultSetMetaData rsm = result.getMetaData();
        columns = new String[rsm.getColumnCount()];
        for (int i = 0; i < rsm.getColumnCount(); i ++) {
            columns[i] = rsm.getColumnName(i+1);
        }

        while (result.next()) {
            rows.add(new Row(result, columns));
        }
    }

    public Row getRow(int index) {
        return rows.get(index);
    }

    public Iterator<Row> getIterator() {
        return rows.stream().iterator();
    }

}
