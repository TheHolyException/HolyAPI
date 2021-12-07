package de.theholyexception.holyapi.util.configreaders;

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
	private String headLine;
	
	public ConfigProperty(File file, String headline) {
		Objects.requireNonNull(file);
		this.file = file;
		this.headLine = (headline == null ? "HolyAPI ConfigProperty" : headLine);
	}	
	
	public void createNew(String pathToDefaultConfig) {
		try {
			properties = new SortedProperties();
			InputStream fileStream = ConfigProperty.class.getClassLoader().getResourceAsStream(pathToDefaultConfig);
			properties.load(fileStream);
			
			FileOutputStream fos = new FileOutputStream(file);
			properties.store(fos, headLine);
			fos.close();
			loadConfig();
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public boolean createNewIfNotExists(String pathToDefaultConfig) {
		if (!file.exists()) {
			createNew(pathToDefaultConfig);
			return true;
		}
		return false;
	}
	
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
	
	public void saveConfig() {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			properties.store(fos, null);
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
	public void setValue(Object path, Object value) {
		if (properties == null) throw new IllegalStateException("Configuration is not loaded");
		properties.put(path, value);
	}

	@Override
	public File getFile() {
		return file;
	}
	
}
