package de.theholyexception.holyapi.datastorage.file;

import java.io.File;
import java.io.InputStream;

public interface FileConfiguration {

	void saveConfig();
	void saveConfig(SaveOption... saveOptions);
	<T> T getValue(Object path, Class<T> clazz);
	<T> T getValue(Object path, T defaultValue, Class<T> clazz);
	void setValue(Object path, Object value);
	boolean createNewIfNotExists();
	boolean createNewIfNotExists(InputStream stream);
	void createNew();
	void createNew(InputStream stream);
	File getFile();
	void loadConfig();

	enum SaveOption {
		PRETTY_PRINT,
	}
	
}
