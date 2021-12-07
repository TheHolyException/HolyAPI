package de.theholyexception.holyapi.util.configreaders;

import java.io.File;

public interface FileConfiguration {

	public void saveConfig();
	public <T> T getValue(Object path, Class<T> clazz);
	public void setValue(Object path, Object value);
	public void createNew(String pathToDefaultConfig);
	public File getFile();
	
}
