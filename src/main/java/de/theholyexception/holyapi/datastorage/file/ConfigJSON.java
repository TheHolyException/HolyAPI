package de.theholyexception.holyapi.datastorage.file;

import de.theholyexception.holyapi.datastorage.json.JSONObjectContainer;
import de.theholyexception.holyapi.datastorage.json.JSONReader;
import de.theholyexception.holyapi.datastorage.json.JSONFormatter;
import de.theholyexception.holyapi.util.DataUtils;
import de.theholyexception.holyapi.util.NotImplementedException;
import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;
import lombok.Getter;

import java.io.*;
import java.util.Arrays;

public class ConfigJSON implements FileConfiguration {
	private final File file;
	@Getter
	private JSONObjectContainer json;

	public ConfigJSON(File file) {
		this.file = file;
	}

	@Override
	public void saveConfig() {
		if (json == null) throw new IllegalStateException("JSON content not loaded!");
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(json.toString().getBytes());
		} catch (IOException ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to safe configuration", ex);
		}
	}

	@Override
	public void saveConfig(SaveOption... saveOptions) {
		if (json == null) throw new IllegalStateException("JSON content not loaded!");
		String output = json.toString();

		if (Arrays.asList(saveOptions).contains(SaveOption.PRETTY_PRINT))
			output = JSONFormatter.format(output);

		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(output.getBytes());
		} catch (IOException ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to safe configuration", ex);
		}
	}

	@Override
	public <T> T getValue(Object path, Class<T> clazz) {throw new NotImplementedException();}
	@Override
	public <T> T getValue(Object path, T defaultValue, Class<T> clazz) {throw new NotImplementedException();}
	@Override
	public void setValue(Object path, Object value) {throw new NotImplementedException();}
	@Override
	public boolean createNewIfNotExists() {
		if (file.exists()) return false;
		createNew();
		return true;
	}
	@Override
	public boolean createNewIfNotExists(InputStream stream) {
		if (file.exists()) return false;
		try {
			boolean result = file.createNewFile();
			if (!result) throw new IllegalStateException("File cant be created!");

			String content = new String(DataUtils.readAllBytes(stream));
			json = (JSONObjectContainer) JSONReader.readString(content);

			saveConfig();
			return true;
		} catch (IOException ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to create configuration file", ex);
		}
		return false;
	}

	@Override
	public void createNew() {
		try {
			boolean result = file.createNewFile();
			if (!result) throw new IllegalStateException("File cant be created!");
		} catch (IOException ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to create configuration file", ex);
		}
	}

	@Override
	public void createNew(InputStream stream) {throw new NotImplementedException();}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public void loadConfig() {
		if (file == null) return;
		json = (JSONObjectContainer) JSONReader.readFile(file);
	}
}
