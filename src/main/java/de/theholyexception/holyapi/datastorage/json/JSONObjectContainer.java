package de.theholyexception.holyapi.datastorage.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONObjectContainer extends JSONContainer implements Serializable, Cloneable {

	private static final long serialVersionUID = -6795984440255441122L;
	
	private final JSONObject data;
	
	protected JSONObjectContainer(JSONObject data) {
		this.data = data;
	}
	
	public <T> T get(Object key, Class<T> clazz) {
		Object o = data.get(key);
		if (o == null) return null;
		if(o instanceof Long && clazz == Integer.class) o = Integer.valueOf((int)((long)o));
		return clazz.cast(o);
	}
	
	public JSONObjectContainer() {
		this.data = new JSONObject();
	}
	
	@SuppressWarnings("unchecked")
	public void set(Object key, Object object) {
		if (object.getClass().isArray()) {
			String className = object.getClass().getName();
			if (className.length() == 2) {
				List<Object> value = new ArrayList<>();
				switch (className.charAt(1)) {
				case 'B':
					for (Object x : (byte[])object) value.add(x);
					break;
				case 'C':
					for (Object x : (char[])object) value.add(x);
					break;
				case 'D':
					for (Object x : (double[])object) value.add(x);
					break;
				case 'F':
					for (Object x : (float[])object) value.add(x);
					break;
				case 'I':
					for (Object x : (int[])object) value.add(x);
					break;
				case 'J':
					for (Object x : (long[])object) value.add(x);
					break;
				case 'S':
					for (Object x : (short[])object) value.add(x);
					break;
				case 'Z':
					for (Object x : (boolean[])object) value.add(x);
					break;
				default:
					break;
				}

				data.put(key, new JSONArray() {{addAll(value);}});
			} else {
				data.put(key, new JSONArray() {{addAll(Arrays.asList((Object[]) object));}});
			}
		} else {
			data.put(key, object);
		}
		if (autoSafe) save(autoSafeFile);
	}
	
	public <T> T get(Object key, T defaultValue, Class<T> clazz) {
		T o = get(key, clazz);
		if (o == null) {
			o = defaultValue;
			set(key, defaultValue);
		}
		return o;
	}

	public JSONObjectContainer getObjectContainer(Object key) {
		if (data.containsKey(key)) {
			return new JSONObjectContainer((JSONObject)data.get(key));
		}
		return null;
	}
	
	public JSONObjectContainer getObjectContainer(Object key, JSONObjectContainer defaultValue) {
		JSONObjectContainer value = getObjectContainer(key);
		if (value == null) {
			value = defaultValue;
			set(key, defaultValue.data);
		}
		return value;
	}
	
	public JSONArrayContainer getArrayContainer(Object key) {
		if (data.containsKey(key)) {
			return new JSONArrayContainer((JSONArray)data.get(key));
		}
		return null;
	}

	public JSONArrayContainer getArrayContainer(Object key, JSONArrayContainer defaultValue) {
		JSONArrayContainer value = getArrayContainer(key);
		if (value == null) {
			value = defaultValue;
			set(key, defaultValue);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Object key) {
		Object object = data.get(key);
		if (object == null) return null;		
		JSONArray array = (JSONArray) data.get(key);
		List<T> list = new ArrayList<>(array.size());
		list.addAll(array);
			
		return list;
	}
	
	public <T> List<T> getList(Object key, List<T> defaultValue) {
		List<T> list = getList(key);
		if (list == null) {
			list = defaultValue;
			set(key, defaultValue);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] getArray(Object key) {
		Object object = data.get(key);
		if (object == null) return null;
		
		JSONArray array = (JSONArray) data.get(key);
		return (T[]) array.toArray();
	}
	
	public <T> T[] getArray(Object key, T[] defaultValue) {
		T[] array = getArray(key);
		if (array == null) {
			array = defaultValue;
			set(key, defaultValue);
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	public <T1,T2> Map<T1, T2> getMap(Object key) {
		JSONObject object = (JSONObject)data.get(key);
		Map<T1,T2> map = new HashMap<T1,T2>(object.size());
		map.putAll(object);
		return map;
	}

	@SuppressWarnings("unchecked")
	public <T1,T2> Map<T1,T2> getMap(Object key, Map<T1,T2> defaultValue) {
		Map<T1, T2> map = getMap(key);
		if (map == null) {
			map = defaultValue;
			JSONObject a = new JSONObject();
			a.putAll(defaultValue);
			set(key, a);
		}
		return map;
	}
	
	public void remove(Object key) {
		data.remove(key);
	}

	@SuppressWarnings("unchecked")
	public <T> void setList(Object key, Collection<T> collection) {
		JSONArray array = new JSONArray();
		array.addAll(collection);
		set(key, array);
	}

	@SuppressWarnings("unchecked")
	public <T1, T2> void setMap(Object key, Map<T1, T2> collection) {
		JSONObject map = new JSONObject();
		map.putAll(collection);
		set(key, map);
	}
	
	public <K,V> Iterator<Map.Entry<K, V>> getIterator() {
		return data.entrySet().iterator();
	}

	@Override
	public JSONObjectContainer clone() throws CloneNotSupportedException {
		return new JSONObjectContainer((JSONObject) data.clone());
	}
	
	@Override
	public JSONObject getRaw() {
		return data;
	}
	
	@Override
	public String toString() {
		return data.toJSONString();
	}	
	
}
