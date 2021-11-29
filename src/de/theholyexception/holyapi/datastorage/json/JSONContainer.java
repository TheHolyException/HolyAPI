package de.theholyexception.holyapi.datastorage.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONAware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import de.theholyexception.holyapi.datastorage.sql.SQLConnection;

public abstract class JSONContainer {

	protected transient boolean prettySave = false;
	protected transient boolean autoSafe = false;
	protected transient File autoSafeFile;
	
	public abstract <T> T getRaw();
	
	
	public <T extends JSONContainer> T prettySave() {
		this.prettySave = true;
		return (T)this;
	}
	
	public <T extends JSONContainer> T autoSafe(File autoSafeFile) {
		this.autoSafe = autoSafeFile != null;
		this.autoSafeFile = autoSafeFile;
		return (T)this;
	}
	
	public void save(File file) {
		JSONAware raw = (JSONAware) getRaw();
		try {
			if (!file.exists()) file.createNewFile();
			if (!this.prettySave) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(raw.toJSONString());
				writer.close();
			} else {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				JsonElement element = new Gson().fromJson(raw.toString(), JsonElement.class);
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				writer.write(gson.toJson(element));
				writer.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void saveSQL(SQLConnection connection, String table, String column) {
		connection.preparedInsert(table, column, ((JSONAware)getRaw()).toJSONString());
	}
	
}
