package de.theholyexception.holyapi.datastorage.json;

import java.io.Serializable;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONArrayContainer extends JSONContainer implements Serializable, Cloneable {

	private static final long serialVersionUID = -3897611615093771174L;
	
	private JSONArray data;
	
	protected JSONArrayContainer(JSONArray data) {
		this.data = data;
	}
	
	public JSONArrayContainer() {
		this.data = new JSONArray();
	}
	
	public void add(Object object) {
		data.add(object);
	}
	
	public <T> void addAll(Collection<T> collection) {
		data.addAll(collection);
	}
	
	public void clear() {
		data.clear();
	}
	
	public Object get(int index) {
		return data.get(index);
	}
	
	public JSONObjectContainer getObjectContainer(int index) {
		return new JSONObjectContainer((JSONObject)this.data.get(index));
	}
	
	public JSONArrayContainer getArrayContainer(int index) {
		return (JSONArrayContainer) data.get(index);
	}
	
	public void remove(int index) {
		data.remove(index);
	}
	
	public void remove(Object object) {
		data.remove(object);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public JSONArray getRaw() {
		return data;
	}
	
	@Override
	public String toString() {
		return data.toJSONString();
	}
}
