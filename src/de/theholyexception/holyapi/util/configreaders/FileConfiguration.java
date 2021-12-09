package de.theholyexception.holyapi.util.configreaders;

import java.io.File;
import java.io.InputStream;

public interface FileConfiguration {

	public void saveConfig();
	public <T> T getValue(Object path, Class<T> clazz);
	public void setValue(Object path, Object value);
	public boolean createNewIfNotExists(InputStream stream);
	public void createNew(InputStream stream);
	public File getFile();
	public void loadConfig();
	
}
