package de.theholyexception.holyapi.datastorage.sql.interfaces;

import de.theholyexception.holyapi.datastorage.sql.Result;
import de.theholyexception.holyapi.util.ExecutorTask;
import de.theholyexception.holyapi.util.NotImplementedException;

import java.sql.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class MSSQLInterface extends DataBaseInterface {

    //region local-variables
    private final String servername;
    private final String instancename;
    private final int portnumber;
    private final String username;
    private final String password;
    private final String database;
    //endregion

    public MSSQLInterface(String servername, String instancename, int portnumber, String username, String password) {
        this.servername = servername;
        this.instancename = instancename;
        this.portnumber = portnumber;
        this.username = username;
        this.password = password;
        this.database = null;
    }

    public MSSQLInterface(String servername, String instancename, int portnumber, String database, String username, String password) {
        this.servername = servername;
        this.instancename = instancename;
        this.portnumber = portnumber;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    @Override
    public void connect() {
        super.connect();
        try {
            String connectionString = String.format("jdbc:sqlserver://%s\\%s:%d;user=%s;password=%s", servername, instancename, portnumber, username, password);
            connection = DriverManager.getConnection(connectionString);
            logger.log(Level.INFO, "Established MySQL Connection.");

            if (database != null) {
                execute("USE " + database);
                ResultSet result = executeQuery("SELECT DB_NAME()");
                result.next();
                if (result.getString(1).equals(database)) {
                    logger.log(Level.INFO, "Switched database to: {0}", database);
                } else
                    logger.log(Level.WARNING, "Failed to switch database, current: {0}; target: {1}", new String[] {result.getString(1), database});
                result.close();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Connection Failed.");
            ex.printStackTrace();
        }
    }

    @Override
    public <T extends DataBaseInterface> T setResultSetConcurrency(int resultSetConcurrency) {
        this.resultSetConcurrency = resultSetConcurrency;
        return (T)this;
    }

    @Override
    public <T extends DataBaseInterface> T setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
        return (T)this;
    }

    //region executeQuery

    @Override
    public ResultSet executeQuery(String query) {
        checkConnection();
        ResultSet result = null;
        try {
            Statement statement = connection.createStatement(resultSetType, resultSetConcurrency);
            result = statement.executeQuery(query);
            statement.closeOnCompletion();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Failed to execute query");
            logger.log(Level.WARNING, ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public ResultSet executeQuerySafe(String query, Object... data) {
        checkConnection();
        int questionMarkCount = 0;
        for (int i = 0; i < query.length(); i ++)
            if (query.charAt(i) == '?') questionMarkCount++;
        if (questionMarkCount != data.length) {
            throw new IllegalArgumentException("Unequal amount of arguments("+data.length+") expected: " + questionMarkCount);
        }
        ResultSet result = null;
        try {
            PreparedStatement statement = connection.prepareStatement(query, resultSetType, resultSetConcurrency);
            for (int i = 0; i < data.length; i ++) {
                statement.setString(i+1, data[i].toString());
            }

            result = statement.executeQuery();
            statement.closeOnCompletion();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Failed to execute query");
            logger.log(Level.WARNING, ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public void executeQueryAsync(Consumer<ResultSet> consumer, String query) {
        if (!allowAsync) throw new IllegalStateException("Async is disabled!");
        checkConnection();
        executorHandler.putTask(new ExecutorTask(() -> consumer.accept(executeQuery(query))));
    }

    @Override
    public void executeQueryAsync(Consumer<ResultSet> consumer, int groupID, String query) {
        if (!allowAsync) throw new IllegalStateException("Async is disabled!");
        checkConnection();
        executorHandler.putTask(new ExecutorTask(() -> consumer.accept(executeQuery(query))), groupID);
    }

    @Override
    public void executeQuerySafeAsync(Consumer<ResultSet> consumer, String query, Object... data) {
        if (!allowAsync) throw new IllegalStateException("Async is disabled!");
        checkConnection();
        executorHandler.putTask(new ExecutorTask(() -> consumer.accept(executeQuerySafe(query, data))));
    }

    @Override
    public void executeQuerySafeAsync(Consumer<ResultSet> consumer, int groupID, String query, Object... data) {
        if (!allowAsync) throw new IllegalStateException("Async is disabled!");
        checkConnection();
        executorHandler.putTask(new ExecutorTask(() -> consumer.accept(executeQuerySafe(query, data)), groupID));
    }
    //endregion

    //region execute
    @Override
    public void execute(String query) {
        checkConnection();
        try (Statement statement = connection.createStatement(resultSetType, resultSetConcurrency)) {
            statement.execute(query);
            if (autoCommit) connection.commit();
            statement.closeOnCompletion();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Failed to execute query");
            logger.log(Level.WARNING, ex.getMessage());
            try {connection.rollback();} catch (SQLException ex1) {ex.printStackTrace();}
        }
    }

    @Override
    public void executeSafe(String query, Object... data) {
        checkConnection();
        int questionMarkCount = 0;
        for (int i = 0; i < query.length(); i ++)
            if (query.charAt(i) == '?') questionMarkCount++;
        if (questionMarkCount != data.length) {
            throw new IllegalArgumentException("Unequal amount of arguments("+data.length+") expected: " + questionMarkCount);
        }
        try {
            PreparedStatement statement = connection.prepareStatement(query, resultSetType, resultSetConcurrency);
            for (int i = 0; i < data.length; i ++) {
                statement.setString(i+1, data[i].toString());
            }

            statement.execute();
            if (autoCommit) connection.commit();
            statement.closeOnCompletion();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Failed to execute query");
            logger.log(Level.WARNING, ex.getMessage());
            if (autoCommit) try {connection.rollback();} catch (SQLException ex1) {ex.printStackTrace();}
        }
    }


    @Override
    public void executeAsync(String query) {
        if (!allowAsync) throw new IllegalStateException("Async is disabled!");
        checkConnection();
        executorHandler.putTask(new ExecutorTask(() -> execute(query)));
    }

    @Override
    public void executeAsync(int groupID, String query) {
        if (!allowAsync) throw new IllegalStateException("Async is disabled!");
        checkConnection();
        executorHandler.putTask(new ExecutorTask(() -> execute(query)), groupID);
    }

    @Override
    public void executeSafeAsync(String query, Object... data) {
        if (!allowAsync) throw new IllegalStateException("Async is disabled!");
        checkConnection();
        executorHandler.putTask(new ExecutorTask(() -> executeSafe(query, data)));
    }

    @Override
    public void executeSafeAsync(int groupID, String query, Object... data) {
        if (!allowAsync) throw new IllegalStateException("Async is disabled!");
        checkConnection();
        executorHandler.putTask(new ExecutorTask(() -> executeSafe(query, data)), groupID);
    }
    //endregion

    //region results
    @Override
    public Result getResult(String query) {
        throw new NotImplementedException();
    }

    @Override
    public Result getResultSafe(String query, Object... data) {
        throw new NotImplementedException();
    }

    @Override
    public void getResultAsync(Consumer<Result> results, String query) {
        throw new NotImplementedException();
    }

    @Override
    public void getResultSafeAsync(Consumer<Result> results, String query, String... data) {
        throw new NotImplementedException();
    }
    //endregion

}
