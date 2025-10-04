package de.theholyexception.holyapi.util;

import de.theholyexception.holyapi.Test;
import de.theholyexception.holyapi.di.DIInject;
import org.json.simple.JSONObject;

public class OtherTest {

	@DIInject
	public Test test;

	@DIInject
	public JSONObject object;

	public Test getTest() {
		return test;
	}

	public JSONObject getObject() {
		return object;
	}
}
