package de.theholyexception.holyapi.datastorage.dataconnection;

import me.kaigermany.utilitys.threads.multithreading.MultiThreadManager;

public abstract class DataInterface {

	public abstract void closeMTM();
	public abstract MultiThreadManager getMTM();
	
}
