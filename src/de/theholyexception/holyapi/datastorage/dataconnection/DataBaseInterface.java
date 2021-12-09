package de.theholyexception.holyapi.datastorage.dataconnection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import de.theholyexception.holyapi.datastorage.dataconnection.models.TableModel;
import me.kaigermany.utilitys.threads.multithreading.MultiThreadManager;

public abstract class DataBaseInterface extends DataInterface {
	
	protected Logger logger; 
	protected Connection connection;
	protected int resultSetType        = ResultSet.TYPE_SCROLL_SENSITIVE;
	protected int resultSetConcurrency = ResultSet.CONCUR_UPDATABLE;
	
	protected boolean autoCommit = true;
	protected boolean allowAsync = false;
	protected MultiThreadManager multiThreadManager = null;

	protected DataBaseInterface() {
	}

	public abstract <T extends DataBaseInterface> T setResultSetType(int resultSetType);
	public abstract <T extends DataBaseInterface> T setResultSetConcurrency(int resultSetConcurrency);

	public DataBaseInterface asyncDataSettings(int threadCount) {
		allowAsync = true;
		multiThreadManager = new MultiThreadManager(threadCount, false);
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
	
	public void connect() {
		if (logger == null) setDefaultLogger();
	}
	
	public void disconnect() {
		try {
			if (connection == null || connection.isClosed()) {
				logger.log(Level.WARNING, "Can't disconnect, there is no valid Connection.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}	
	
	@Override
	public void closeMTM() {
		if (allowAsync)
			multiThreadManager.stop();
	}

	@Override
	public MultiThreadManager getMTM() {
		return allowAsync ? multiThreadManager : null;
	}
	
	public Logger getLogger() {
		return logger;
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
	
	public abstract void 		createTable				(TableModel model, String name);
	public abstract void		updateTable				(TableModel model, String name);
	public abstract TableModel	getTable				(String name);
	
	class ExecuteBuilder {
		
		String sql;
		private boolean async = false;
		private List<String> safeArgs;
		public ExecuteBuilder(String sql) {
			this.sql = sql;
			safeArgs = new ArrayList<>();
		}
		
		public ExecuteBuilder async() {
			if (!allowAsync) throw new IllegalStateException("Async is disabled!");
			this.async = true;
			return this;
		}
		
	}
	
	class QueryBuilder {
		
	}
	
}
