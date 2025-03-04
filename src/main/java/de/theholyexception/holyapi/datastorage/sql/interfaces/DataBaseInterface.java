package de.theholyexception.holyapi.datastorage.sql.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import de.theholyexception.holyapi.datastorage.sql.Result;
import de.theholyexception.holyapi.util.ExecutorHandler;
import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;
import lombok.Getter;

public abstract class DataBaseInterface {

	@Getter
	protected Connection connection;
	protected int resultSetType        = ResultSet.TYPE_SCROLL_SENSITIVE;
	protected int resultSetConcurrency = ResultSet.CONCUR_UPDATABLE;
	
	protected boolean autoCommit = true;
	protected boolean allowAsync = false;
	@Getter
	protected ExecutorHandler executorHandler = null;

	protected DataBaseInterface() {
	}

	public abstract <T extends DataBaseInterface> T setResultSetType(int resultSetType);
	public abstract <T extends DataBaseInterface> T setResultSetConcurrency(int resultSetConcurrency);

	public DataBaseInterface asyncDataSettings(int threadCount) {
		allowAsync = true;
		executorHandler = new ExecutorHandler(Executors.newFixedThreadPool(threadCount));
		return this;
	}
	
	public DataBaseInterface setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
		return this;
	}

	public void checkConnection() {
		try {
			if (connection == null || connection.isClosed())
				connect();
		} catch (SQLException ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to check connection", ex);
		}
	}
	public abstract void connect();
	
	public void disconnect() {
		try {
			if (connection == null || connection.isClosed()) {
				LoggerProxy.log(LogLevel.WARN,"Can't disconnect, there is no valid Connection.");
			}

			if (allowAsync) {
				LoggerProxy.log(LogLevel.INFO,"Awaiting async tasks.");
				executorHandler.closeAfterExecution();
			}
		} catch (Exception ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to disconnect", ex);
		}
	}

	public void logServerInfos() {
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			LoggerProxy.log(LogLevel.INFO,"Established Connection.");
			LoggerProxy.log(LogLevel.INFO,"Driver name: {0}", metaData.getDriverName());
			LoggerProxy.log(LogLevel.INFO,"Driver version: {0}", metaData.getDriverVersion());
			LoggerProxy.log(LogLevel.INFO,"Product name: {0}", metaData.getDatabaseProductName());
			LoggerProxy.log(LogLevel.INFO,"Product version: {0}", metaData.getDatabaseProductVersion());
		} catch (SQLException ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to log server infos", ex);
		}
	}

	public abstract ResultSet 	executeQuery			(String query);
	public abstract ResultSet 	executeQuerySafe		(String query, Object... data);
	public abstract void 		executeQueryAsync		(Consumer<ResultSet> consumer, String query);
	public abstract void 		executeQueryAsync		(Consumer<ResultSet> consumer, int groupID, String query);
	public abstract void 		executeQuerySafeAsync	(Consumer<ResultSet> consumer, String query, Object... data);
	public abstract void 		executeQuerySafeAsync	(Consumer<ResultSet> consumer, int groupID, String query, Object... data);
	
	public abstract void        execute                 (String query);
	public abstract void        executeSafe             (String query, Object... data);
	public abstract void        executeAsync            (String query);
	public abstract void        executeAsync            (int groupID, String query);
	public abstract void        executeSafeAsync        (String query, Object... data);
	public abstract void        executeSafeAsync        (int groupID, String query, Object... data);

	public abstract Result getResult			(String query);
	public abstract Result getResultSafe   		(String query, Object... data);
	public abstract void getResultAsync      	(Consumer<Result> results, String query);
	public abstract void getResultSafeAsync  	(Consumer<Result> results, String query, String... data);

	
	protected class ExecuteBuilder {
		private boolean async = false;
		private boolean batched = false;
		private List<PreparedStatement> batchedStatements;
		private List<String> arguments = new ArrayList<>();
		
		public ExecuteBuilder setAsync() {
			if (!allowAsync) throw new IllegalStateException("Async is disabled!");
			this.async = true;
			return this;
		}
		
		public ExecuteBuilder setBatched() {
			batched = true;
			this.batchedStatements = new ArrayList<>();
			return this;
		}
		
		public void setArguments(String... args) {
			for (int i = 0; i < args.length; i ++) arguments.add(args[i]);
		}
		
		public void execute() {}
	}
	
	protected class QueryBuilder implements Closeable {

		@Override
		public void close() throws IOException {
		}
		
	}
	
}
