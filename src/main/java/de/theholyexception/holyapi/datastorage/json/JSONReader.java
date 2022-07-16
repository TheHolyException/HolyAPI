package de.theholyexception.holyapi.datastorage.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONReader {

	protected static Map<JSONReader, File> autoSaves = new HashMap<>();
	private static Logger logger = Logger.getLogger(JSONReader.class.getName());
	
	
	public static JSONContainer readFile(File file) {
		try {
			Object object;
			if (file.length() == 0) {
				object = new JSONObject();
				
			} else {
				FileReader reader = new FileReader(file);
				object = new JSONParser().parse(reader);
			}
			if (object instanceof JSONObject) {
				return new JSONObjectContainer((JSONObject)object);
			} else if (object instanceof JSONArray) {
				return new JSONArrayContainer((JSONArray)object);
			}
			logger.log(Level.FINER, "Failed to parse File");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static JSONContainer readString(String jsondata) {
		try {
			Object object = new JSONParser().parse(jsondata);
			if (object instanceof JSONObject) {
				return new JSONObjectContainer((JSONObject)object);
			} else if (object instanceof JSONArray) {
				return new JSONArrayContainer((JSONArray)object);
			}
			logger.log(Level.FINER, "Failed to parse File");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static JSONContainer readURL(URL url) {
		try {
			InputStream is 			= url.openStream();
			BufferedReader reader 	= new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			StringBuilder builder 	= new StringBuilder();
			int inputLine;
			while((inputLine = reader.read()) != -1) {
				builder.append((char)inputLine);
			}
			Object object = new JSONParser().parse(builder.toString());
			if (object instanceof JSONObject) {
				return new JSONObjectContainer((JSONObject)object);
			} else if (object instanceof JSONArray) {
				return new JSONArrayContainer((JSONArray)object);
			}
			logger.log(Level.FINER, "Failed to parse File");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	


	
}
