package de.theholyexception.holyapi.datastorage.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;

import de.theholyexception.holyapi.HolyAPI;

public class SQLConnection {
	
	//region localvairables
	private final Mode mode;
	private String host;
	private String port;
	private String database;
	private String username;
	private String password;
	private String sqllitefile;
	
	public Connection connection;
	//endregion localvairables
	
	public SQLConnection(String host, String port, String database, String username, String password) {
		this.mode = Mode.MySQL;
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
	}
	
	public SQLConnection(String sqllitefile) {
		this.mode = Mode.SQLLite;
		this.sqllitefile = sqllitefile;
	}
	
	public void connect() {
		try {
			HolyAPI.logger.log(Level.INFO,"Establishing MySQL Connection.");
			switch(mode) {
			case MySQL:
				connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database+"?autoReconnect=true", username, password);
				break;
			case SQLLite:
				connection = DriverManager.getConnection("jdbc:sqlite:"+sqllitefile);
				break;
			}
			HolyAPI.logger.log(Level.INFO,"Connection established.");
		} catch (SQLException ex) {
			HolyAPI.logger.log(Level.SEVERE, "Connection failed.");
		}
	}
	
	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
				HolyAPI.logger.log(Level.INFO, "SQL Connection Closed.");
			} catch (SQLException ex) {
				HolyAPI.logger.log(Level.SEVERE, "Failed to close Connection: " + ex.getMessage());
			}
		}
	}
	
	public boolean isConnected() {
		return connection != null;
	}
	
	public synchronized void preparedInsert(String table, String... data) {
		try {
			if(data.length == 0 || (data.length & 1) == 1) return;
			StringBuilder sb1 = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			for(int i=0; i<data.length; i+=2) {
				sb1.append(",`").append(data[i]).append('`');
				sb2.append(",?");
			}
			String a = "(" + sb1.toString().substring(1) + ")";
			String b = "(" + sb2.toString().substring(1) + ")";
			PreparedStatement cmd = connection.prepareStatement("INSERT INTO `"+table+"` "+a+" VALUES "+b, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (mode == Mode.MySQL) cmd.setFetchSize(Integer.MIN_VALUE);
			int ii=1;
			for(int i=1; i<data.length; i+=2) {
				cmd.setString(ii++, data[i]);
			}
			
			cmd.executeUpdate();
			cmd.closeOnCompletion();
			cmd.close();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public synchronized void preparedUpdate(String table, String... data) {
		try {
			if(data.length == 0 || (data.length & 1) == 1) return;
			StringBuilder sb1 = new StringBuilder();
			for(int i=0; i<data.length; i+=2) {
				sb1.append(",`").append(data[i]).append('`').append("=?");
			}
			String a = "(" + sb1.toString().substring(1) + ")";
			PreparedStatement cmd = connection.prepareStatement("UPDATE `"+table+"` SET "+a,
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			cmd.setFetchSize(Integer.MIN_VALUE);
			int ii=1;
			for(int i=1; i<data.length; i+=2) {
				cmd.setString(ii++, data[i]);
			}
			
			cmd.executeUpdate();
			cmd.closeOnCompletion();
			cmd.close();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public ResultSet query(String qry) {
		ResultSet rs = null;
		try {
			Statement st = connection.createStatement(
					(mode == Mode.MySQL ? ResultSet.TYPE_SCROLL_SENSITIVE : ResultSet.TYPE_FORWARD_ONLY),
					(mode == Mode.MySQL ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY)
					);
			if (mode == Mode.MySQL) st.setFetchSize(Integer.MIN_VALUE);
			rs = st.executeQuery(qry);
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
		return rs;
	}
	
	public void update(String qry) {
		try {
			Statement st = connection.createStatement(
					(mode == Mode.MySQL ? ResultSet.TYPE_SCROLL_SENSITIVE : ResultSet.TYPE_FORWARD_ONLY),
					(mode == Mode.MySQL ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY)
					);
			st.executeUpdate(qry);
			st.close();
		} catch (SQLException e) {
			connect();
			System.out.println("s");
			System.err.println(e);
		}
	}
	
	public void createTable(String name, String[] columns) {
		
	}
	
	public void createColum(String table, String colum, String datatype, String defaultvalue) {
		if (isConnected()) {
			try {
				connection.createStatement().executeUpdate(
						"ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS " + colum
						+ " " + datatype + " DEFAULT '" + defaultvalue + "'");
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		} else {
			HolyAPI.logger.log(Level.SEVERE, "No Database Connection!");
		}
	}
	
	public boolean contains(String table, String keyName, String keyValue) {
		try {
			ResultSet resultSet = query("SELECT * FROM `" + table + "` WHERE `" + keyName + "`='" + keyValue + "' LIMIT 0, 1");
			boolean b = resultSet.first();
			resultSet.close();
			return b;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	private volatile HashMap<String, PreparedStatement> cmd = new HashMap<>();
	public void preparedBatchedStatment(String identifier, String sql) {		
		try {			
			 PreparedStatement statment = cmd.get(identifier);
			if (statment == null) {
				cmd.put(identifier, statment = connection.prepareStatement(sql));
			} else {
				statment.addBatch(sql);
			}			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}


	public boolean flushBatch(String identifier) {
		try {
			PreparedStatement statment = cmd.get(identifier);
			if (statment == null) return false;
			synchronized (this) {
				statment.executeBatch();
				statment.closeOnCompletion();
				statment.close();
				cmd.remove(identifier);
			}
			return true;
		} catch (SQLException ex) {
			 ex.printStackTrace();
		}
		return false;
	}
	
	public enum Mode {
		MySQL,
		SQLLite;
	}
}
