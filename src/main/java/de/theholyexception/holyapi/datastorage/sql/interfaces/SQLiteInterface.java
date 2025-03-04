package de.theholyexception.holyapi.datastorage.sql.interfaces;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;
import java.util.logging.Level;

import de.theholyexception.holyapi.datastorage.sql.Result;
import de.theholyexception.holyapi.util.ExecutorTask;
import de.theholyexception.holyapi.util.NotImplementedException;
import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;

public class SQLiteInterface extends DataBaseInterface {

	//region local-variables
	private final String path;
	//endregion
	
	public SQLiteInterface(File file) {
		try {
			if (!file.exists()) file.createNewFile();
		} catch (IOException ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to create SQLite file", ex);
		} finally {
			path = file.getAbsolutePath();
		}
		this.resultSetType = ResultSet.TYPE_FORWARD_ONLY;
		this.resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
		this.autoCommit = false;
	}
	
	@Override
	public void connect() {
		try {
			LoggerProxy.log(LogLevel.INFO, "Establishing SQLite Connection.");
			connection = DriverManager.getConnection("jdbc:sqlite:"+path);
			logServerInfos();
		} catch (SQLException ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to connect to SQLite", ex);
		}
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
		LoggerProxy.log(LogLevel.INFO, "Closing SQLite connection.");
		try {
			connection.close();
			LoggerProxy.log(LogLevel.INFO, "Connection closed.");
		} catch (Exception ex) {
			LoggerProxy.log(LogLevel.ERROR, "Closing connection Failed.", ex);
		}
	}
	
	@Override
	public <T extends DataBaseInterface> T setResultSetConcurrency(int resultSetConcurrency) {
		try {
			throw new UnsupportedOperationException("SQLite only supports CONCUR_READ_ONLY cursors");
		} catch (UnsupportedOperationException ex) {
			LoggerProxy.log(LogLevel.ERROR, ex.getClass().getName() + " " + ex.getMessage());
		}
		return (T)this;
	}
	
	@Override
	public <T extends DataBaseInterface> T setResultSetType(int resultSetType) {
		try {
			throw new UnsupportedOperationException("SQLite only supports TYPE_FORWARD_ONLY cursors");
		} catch (UnsupportedOperationException ex) {
			LoggerProxy.log(LogLevel.ERROR, ex.getClass().getName() + " " + ex.getMessage());
		}
		return (T)this;
	}

	//region executequery
	@Override
	public ResultSet executeQuery(String query) {
		checkConnection();
		ResultSet result = null;
		try {
			Statement statement = connection.createStatement(resultSetType, resultSetConcurrency);
			result = statement.executeQuery(query);
			statement.closeOnCompletion();
		} catch (SQLException ex) {
			LoggerProxy.log(LogLevel.ERROR, "Failed to execute query", ex);
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
			LoggerProxy.log(LogLevel.ERROR, "Failed to execute query", ex);
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
			statement.closeOnCompletion();
		} catch (SQLException ex) {
			LoggerProxy.log(LogLevel.ERROR, "Failed to execute query", ex);
			try {connection.rollback();} catch (SQLException ex1) {LoggerProxy.log(LogLevel.ERROR, "Failed to rollback changes", ex1);}
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
		try (PreparedStatement statement = connection.prepareStatement(query, resultSetType, resultSetConcurrency)) {
			for (int i = 0; i < data.length; i ++) {
				statement.setString(i+1, data[i].toString());
			}

			statement.execute();
			statement.closeOnCompletion();
		} catch (SQLException ex) {
			LoggerProxy.log(LogLevel.ERROR, "Failed to execute query", ex);
			try {connection.rollback();} catch (SQLException ex1) {LoggerProxy.log(LogLevel.ERROR, "Failed to rollback changes", ex1);}
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
