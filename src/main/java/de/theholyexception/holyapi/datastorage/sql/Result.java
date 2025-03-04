package de.theholyexception.holyapi.datastorage.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Result {

    private final List<Table> tables = new ArrayList<>();

    public Result(PreparedStatement statement) throws SQLException {
        boolean hasResults;
        do {
            ResultSet rs = statement.getResultSet();
            tables.add(new Table(rs));
            hasResults = statement.getMoreResults();
        } while (hasResults);
        statement.close();
    }

    public Table getTable(int index) {
        return tables.get(index);
    }

}
