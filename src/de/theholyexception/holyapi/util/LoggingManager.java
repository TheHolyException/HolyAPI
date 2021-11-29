package de.theholyexception.holyapi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.theholyexception.holyapi.datastorage.dataconnection.DataBaseInterface;

public class LoggingManager {
	
	private static final String CURRENT_LOGNAME = "latest.log";
	
	private int day = -1;
	private String identification;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private File logFolder;
	private Logger logger;
	private FileHandler handler;
	
	@SuppressWarnings("deprecation")
	public LoggingManager(File logFolder, String identification) {
		this.logFolder = logFolder;
		this.identification = identification;
		if (!logFolder.exists()) logFolder.mkdirs();
		
		day = new Date(System.currentTimeMillis()).getDay();
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		

		archiveLogfile(new File(logFolder, CURRENT_LOGNAME));
		setupLogger();
		day = cal.get(Calendar.DAY_OF_YEAR);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				cal.setTime(new Date());
				if (day != cal.get(Calendar.DAY_OF_YEAR)) {
					day = cal.get(Calendar.DAY_OF_YEAR);
					handler.close();
					logger.removeHandler(handler);
					archiveLogfile(new File(logFolder, CURRENT_LOGNAME));
					setupLogger();
				}
			}
		}, 10000, 60000);
	}
	
	private void setupLogger() {
		try {
			File logFile = new File(logFolder, CURRENT_LOGNAME);
			if (!logFile.exists()) logFile.createNewFile();
			InputStream logpropstream = DataBaseInterface.class.getClassLoader().getResourceAsStream("logging.properties");
			LogManager.getLogManager().readConfiguration(logpropstream);
			logger = Logger.getLogger(identification);
			handler = new FileHandler(logFile.getAbsolutePath());
			logger.addHandler(handler);
			
			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void archiveLogfile(File logfile) {
		if (!logfile.exists()) {
			System.out.println("Cannot archive LogFile, there is no " + logfile.getAbsolutePath());
			return;
		}
		
		File targetFile = new File(logFolder, dateFormat.format(new Date())+"-1.log.zip");
		int iteration = 1;
		while (targetFile.exists()) {
			targetFile = new File(logFolder, dateFormat.format(new Date())+"-"+(++iteration)+".log.zip");
		}
		
		System.out.println("Archive to " + targetFile.getAbsolutePath());
		
		try {
			FileInputStream fis = new FileInputStream(logfile);
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetFile));
			zos.putNextEntry(new ZipEntry(logfile.getName()));
			byte[] buffer = new byte[1024];
			int a;
			while((a = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, a);
			}
			zos.close();
			fis.close();
			Files.delete(logfile.toPath());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	public Logger getLogger() {
		return logger;
	}

}
