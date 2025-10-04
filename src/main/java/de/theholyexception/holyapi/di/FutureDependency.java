package de.theholyexception.holyapi.di;

import java.lang.reflect.Field;

public class FutureDependency {

	private Object instance;
	private Field field;

	public FutureDependency(Object instance, Field field) {
		this.instance = instance;
		this.field = field;
	}

	public Object getInstance() {
		return instance;
	}

	public Field getField() {
		return field;
	}
}
