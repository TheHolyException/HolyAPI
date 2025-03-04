package de.theholyexception.holyapi.datastorage.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;
import org.json.simple.JSONAware;

public abstract class JSONContainer {

	protected boolean prettySave = false;
	protected boolean autoSafe = false;
	protected File autoSafeFile;
	
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
		JSONAware raw = getRaw();
		try {
			if (!file.exists()) file.createNewFile();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(this.prettySave ? JSONFormatter.format(raw.toJSONString()) : raw.toJSONString());
			}
		} catch (IOException ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to save JSONContainer to file", ex);
		}
	}
	
}
