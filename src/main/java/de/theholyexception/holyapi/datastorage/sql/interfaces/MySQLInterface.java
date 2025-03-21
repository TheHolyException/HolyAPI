package de.theholyexception.holyapi.datastorage.sql.interfaces;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import de.theholyexception.holyapi.datastorage.sql.Result;
import de.theholyexception.holyapi.util.ExecutorTask;
import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;

public class MySQLInterface extends DataBaseInterface {

	//region local-variables
	private final String address;
	private final int port;
	private final String database;
	private final String username;
	private final String password;
	//endregion
	
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (Exception ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to load MySQL Driver", ex);
		}
	}
	public MySQLInterface(String address, int port, String username, String password, String database) {
		this.address = address;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				try {
					if (connection.isClosed()) {
						connect();
					}
				} catch (SQLException e) {
					LoggerProxy.log(LogLevel.ERROR, "Failed to reconnect", e);
				}
			}
		}, 10000, 10000);
	}
	
	
	@Override
	public void connect() {
		try {
			LoggerProxy.log(LogLevel.INFO, "Establishing MySQL Connection.");
			connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s", address, port, database), username, password);
			connection.setAutoCommit(false);
			logServerInfos();
		} catch (Exception ex) {
			LoggerProxy.log(LogLevel.ERROR, "Connection Failed.", ex);
		}
	}

	@Override
	public void disconnect() {
		super.disconnect();
		LoggerProxy.log(LogLevel.INFO, "Closing MySQL connection.");
		try {
			connection.close();
			LoggerProxy.log(LogLevel.INFO, "Connection closed.");
		} catch (Exception ex) {
			LoggerProxy.log(LogLevel.ERROR, "Closing connection Failed.");
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
			if (autoCommit) connection.commit();
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
		try {
			PreparedStatement statement = connection.prepareStatement(query, resultSetType, resultSetConcurrency);
			for (int i = 0; i < data.length; i ++) {
				statement.setString(i+1, data[i].toString());
			}

			statement.execute();
			if (autoCommit) connection.commit();
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
		checkConnection();

		try (PreparedStatement statement = connection.prepareStatement(query, resultSetType, resultSetConcurrency)) {
			statement.closeOnCompletion();
			boolean results = statement.execute(query);

			if (!results)
				return null;

			Result result = new Result(statement);
			if (autoCommit) connection.commit();
			return result;
		} catch (SQLException ex) {
			LoggerProxy.log(LogLevel.ERROR, "Failed to execute query", ex);
		}
		return null;
	}

	@Override
	public Result getResultSafe(String query, Object... data) {
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

			boolean results = statement.execute(query);

			if (!results)
				return null;

			Result result = new Result(statement);
			if (autoCommit) connection.commit();
			return result;
		} catch (SQLException ex) {
			LoggerProxy.log(LogLevel.ERROR, "Failed to execute query", ex);
		}
		return null;
	}

	@Override
	public void getResultAsync(Consumer<Result> results, String query) {
		if (!allowAsync) throw new IllegalStateException("Async is disabled!");
		checkConnection();
		executorHandler.putTask(new ExecutorTask(() -> results.accept(getResult(query))));
	}

	@Override
	public void getResultSafeAsync(Consumer<Result> results, String query, String... data) {
		if (!allowAsync) throw new IllegalStateException("Async is disabled!");
		checkConnection();
		executorHandler.putTask(new ExecutorTask(() -> results.accept(getResultSafe(query, data))));
	}
	//endregion

}
