package de.theholyexception.holyapi.datastorage.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import de.theholyexception.holyapi.util.NotImplementedException;
import de.theholyexception.holyapi.util.SortedProperties;
import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;

public class ConfigProperty implements FileConfiguration {

	private final File file;
	private Properties properties;
	private final String headline;
	
	public ConfigProperty(File file, String headline) {
		Objects.requireNonNull(file);
		this.file = file;
		this.headline = (headline == null ? "HolyAPI ConfigProperty" : headline);
		properties = new SortedProperties();
	}
	@Override
	public void createNew() {
		try {
			file.createNewFile();
			try (FileOutputStream os = new FileOutputStream(file)) {
				properties.store(os, headline);
			}
		} catch (Exception ex) {
			LoggerProxy.log(LogLevel.ERROR, "Failed to create configuration file", ex);
		}
	}
	@Override
	public void createNew(InputStream stream) {
		try {
			properties = new SortedProperties();
			properties.load(stream);

			try (FileOutputStream fos = new FileOutputStream(file)) {
				properties.store(fos, headline);
			}
			loadConfig();
		} catch (Exception ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to create configuration file", ex);
		}		
	}

	@Override
	public boolean createNewIfNotExists() {
		if (!file.exists()) {
			createNew();
			return true;
		}
		return false;
	}

	@Override
	public boolean createNewIfNotExists(InputStream stream) {
		if (!file.exists()) {
			createNew(stream);
			return true;
		}
		return false;
	}

	@Override
	public void loadConfig() {
		try {
			properties = new SortedProperties();
			try (FileInputStream fis = new FileInputStream(file)) {
				properties.load(fis);
			}
		} catch (Exception ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to load configuration file", ex);
		}
	}

	@Override
	public void saveConfig() {
		try {
			try (FileOutputStream fos = new FileOutputStream(file)) {
				properties.store(fos, null);
			}
		} catch (Exception ex) {
			LoggerProxy.log(LogLevel.ERROR,"Failed to safe configuration", ex);
		}
	}

	@Override
	public void saveConfig(SaveOption... saveOptions) {
		throw new NotImplementedException();
	}

	@Override
	public <T> T getValue(Object path, Class<T> clazz) {
		if (properties == null) throw new IllegalStateException("Configuration is not loaded");
		return clazz.cast(properties.get(path));
	}

	@Override
	public <T> T getValue(Object path, T defaultValue, Class<T> clazz) {
		if (properties == null) throw new IllegalStateException("Configuration is not loaded");
		if (!properties.containsKey(path)) properties.put(path, clazz.cast(defaultValue));
		return clazz.cast(properties.get(path));
	}

	@Override
	public void setValue(Object path, Object value) {
		if (properties == null) throw new IllegalStateException("Configuration is not loaded");
		properties.put(path, value);
	}

	@Override
	public File getFile() {
		return file;
	}
	
}
