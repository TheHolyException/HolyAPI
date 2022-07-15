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

import de.theholyexception.holyapi.util.ExecutorHandler;

public abstract class DataBaseInterface {

	protected Logger logger;
	protected Connection connection;
	protected int resultSetType        = ResultSet.TYPE_SCROLL_SENSITIVE;
	protected int resultSetConcurrency = ResultSet.CONCUR_UPDATABLE;
	
	protected boolean autoCommit = true;
	protected boolean allowAsync = false;
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

	public DataBaseInterface setLogger(Logger logger) {
		this.logger = logger;
		return this;
	}
	
	public DataBaseInterface setDefaultLogger() {
		try {
			this.logger = Logger.getLogger("HolyAPI");
			InputStream stream = DataBaseInterface.class.getClassLoader().getResourceAsStream("logging.properties");
			LogManager.getLogManager().readConfiguration(stream);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public void checkConnection() {
		try {
			if (connection == null || connection.isClosed())
				connect();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	public void connect() {
		if (logger == null) setDefaultLogger();
	}
	
	public void disconnect() {
		try {
			if (connection == null || connection.isClosed()) {
				logger.log(Level.WARNING, "Can't disconnect, there is no valid Connection.");
			}

			if (allowAsync) {
				logger.log(Level.INFO, "Awaiting async tasks.");
				executorHandler.closeAfterExecution();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void logServerInfos() {
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			logger.log(Level.INFO, "Established Connection.");
			logger.log(Level.INFO, "Driver name: {0}", metaData.getDriverName());
			logger.log(Level.INFO, "Driver version: {0}", metaData.getDriverVersion());
			logger.log(Level.INFO, "Product name: {0}", metaData.getDatabaseProductName());
			logger.log(Level.INFO, "Product version: {0}", metaData.getDatabaseProductVersion());
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public Logger getLogger() {
		return logger;
	}

	public ExecutorHandler getExecutorHandler() {
		return executorHandler;
	}

	public abstract ResultSet 	executeQuery			(String query);
	public abstract ResultSet 	executeQuerySafe		(String query, String... data);
	public abstract void 		executeQueryAsync		(Consumer<ResultSet> consumer, String query);
	public abstract void 		executeQueryAsync		(Consumer<ResultSet> consumer, int groupID, String query);
	public abstract void 		executeQuerySafeAsync	(Consumer<ResultSet> consumer, String query, String... data);
	public abstract void 		executeQuerySafeAsync	(Consumer<ResultSet> consumer, int groupID, String query, String... data);
	
	public abstract void        execute                 (String query);
	public abstract void        executeSafe             (String query, String... data);
	public abstract void        executeAsync            (String query);
	public abstract void        executeAsync            (int groupID, String query);
	public abstract void        executeSafeAsync        (String query, String... data);
	public abstract void        executeSafeAsync        (int groupID, String query, String... data);
	
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
