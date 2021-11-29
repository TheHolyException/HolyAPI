package de.theholyexception.holyapi.datastorage.dataconnection.interfaces;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;
import java.util.logging.Level;

import de.theholyexception.holyapi.datastorage.dataconnection.DataBaseInterface;
import de.theholyexception.holyapi.datastorage.dataconnection.models.TableModel;
import de.theholyexception.holyapi.util.exceptions.NotImplementedException;
import me.kaigermany.utilitys.threads.multithreading.JobFunction;

public class MySQLInterface extends DataBaseInterface {

	//region localvariables
	private String address;
	private int port;
	private String database;
	private String username;
	private String password;
	
	private JobFunction executeQueryJob = new JobFunction() {
		@Override
		public Object[] run(Object[] input) {
			Consumer<ResultSet> consumer = (Consumer<ResultSet>) input[1];
			if (input.length >= 3)
				consumer.accept(executeQuerySafe(input[0].toString(), (String[])input[2]));
			else
				consumer.accept(executeQuery(input[0].toString()));
			return null;
		}
	};
	
	private JobFunction executeJob = new JobFunction() {
		@Override
		public Object[] run(Object[] input) {
			if (input.length >= 2)
				executeSafe(input[0].toString(), (String[])input[1]);
			else
				execute(input[0].toString());
			return null;
		}
	};
	//endregion
	
	
	public MySQLInterface(String address, int port, String username, String password, String database) {
		this.address = address;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
	}
	
	
	@Override
	public void connect() {
		super.connect();
		try {
			logger.log(Level.INFO, "Establishing MySQL Connection.");
			connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s", address, port, database), username, password);
			connection.setAutoCommit(false);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Connection Failed.");
			ex.printStackTrace();
		} finally {
			logger.log(Level.INFO, "Established MySQL Connection.");
		}
	}

	@Override
	public void disconnect() {
		super.disconnect();
		logger.log(Level.INFO, "Closing MySQL connection.");
		if (allowAsync) {
			logger.log(Level.INFO, "Awaiting async tasks.");
			multiThreadManager.awaitAll();
			multiThreadManager.stop();
		}
		try {
			connection.close();
			logger.log(Level.INFO, "Connection closed.");
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Closing connection Failed.");
		} finally {
			logger.log(Level.INFO, "Closed MySQL Connection.");
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
		ResultSet result = null;
		try {
			Statement statement = connection.createStatement(resultSetType, resultSetConcurrency);
			result = statement.executeQuery(query);
			statement.closeOnCompletion();
		} catch (SQLException ex) {
			logger.log(Level.WARNING, "Failed to execute query");
			logger.log(Level.WARNING, ex.getMessage());
		}
		return result;
	}

	@Override
	public ResultSet executeQuerySafe(String query, String... data) {
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
				statement.setString(i+1, data[i]);
			}
			
			result = statement.executeQuery();
			statement.closeOnCompletion();
		} catch (SQLException ex) {
			logger.log(Level.WARNING, "Failed to execute query");
			logger.log(Level.WARNING, ex.getMessage());
		}
		return result;
	}

	@Override
	public void executeQueryAsync(Consumer<ResultSet> consumer, String query) {
		if (!allowAsync) throw new IllegalStateException("Async is disabled!");
		multiThreadManager.putJob(executeQueryJob, query, consumer);
	}

	@Override
	public void executeQuerySafeAsync(Consumer<ResultSet> consumer, String query, String... data) {
		if (!allowAsync) throw new IllegalStateException("Async is disabled!");
		multiThreadManager.putJob(executeQueryJob, query, consumer, data);
	}
	//endregion
	
	//region execute
	@Override
	public void execute(String query) {
		try {
			Statement statement = connection.createStatement(resultSetType, resultSetConcurrency);
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
	public void executeSafe(String query, String... data) {
		int questionMarkCount = 0;
		for (int i = 0; i < query.length(); i ++)
			if (query.charAt(i) == '?') questionMarkCount++;
		if (questionMarkCount != data.length) {
			throw new IllegalArgumentException("Unequal amount of arguments("+data.length+") expected: " + questionMarkCount);
		}
		try {
			PreparedStatement statement = connection.prepareStatement(query, resultSetType, resultSetConcurrency);
			for (int i = 0; i < data.length; i ++) {
				statement.setString(i+1, data[i]);
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
		multiThreadManager.putJob(executeJob, query);
	}
	
	@Override
	public void executeSafeAsync(String query, String... data) {
		if (!allowAsync) throw new IllegalStateException("Async is disabled!");
		multiThreadManager.putJob(executeJob, query, data);
	}
	//endregion

	//region tables
	@Override
	public void createTable(TableModel model, String name) {
		throw new NotImplementedException("not implemented");
	}
	
	@Override
	public void updateTable(TableModel model, String name) {
		throw new NotImplementedException("not implemented");
	}
	
	@Override
	public TableModel getTable(String name) {
		throw new NotImplementedException("not implemented");
		/*TableModel model = null;
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM " + name);
			model = new TableModel(rs);
			rs.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return model;*/
	}
	//endregion

}
