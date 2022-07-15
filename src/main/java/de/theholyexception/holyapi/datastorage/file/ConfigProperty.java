package de.theholyexception.holyapi.datastorage.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import de.theholyexception.holyapi.util.SortedProperties;

public class ConfigProperty implements FileConfiguration {

	private File file;
	private Properties properties;
	private String headline;
	
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
			FileOutputStream os = new FileOutputStream(file);
			properties.store(os, headline);
			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Override
	public void createNew(InputStream stream) {
		try {
			properties = new SortedProperties();
			properties.load(stream);
			
			FileOutputStream fos = new FileOutputStream(file);
			properties.store(fos, headline);
			fos.close();
			loadConfig();
		} catch (Exception ex) {
			ex.printStackTrace();
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
			FileInputStream fis = new FileInputStream(file);
			properties = new SortedProperties();			
			properties.load(fis);
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void saveConfig() {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			properties.store(fos, null);
			fos.flush();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
