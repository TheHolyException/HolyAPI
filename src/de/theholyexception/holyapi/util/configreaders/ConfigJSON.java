package de.theholyexception.holyapi.util.configreaders;

import java.io.File;
import java.io.InputStream;

public class ConfigJSON implements FileConfiguration {

	@Override
	public void saveConfig() {
	}

	@Override
	public <T> T getValue(Object path, Class<T> clazz) {
		return null;
	}

	@Override
	public void setValue(Object path, Object value) {
	}

	@Override
	public boolean createNewIfNotExists(InputStream stream) {
		return false;
	}

	@Override
	public void createNew(InputStream stream) {
	}

	@Override
	public File getFile() {
		return null;
	}

	@Override
	public void loadConfig() {
	}
	
}
