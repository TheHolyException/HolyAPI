package de.theholyexception.holyapi.di;

import java.lang.reflect.Field;

@Deprecated
public class AdvancedDIContainer extends SimpleDIContainer {
	@Override
	public <T> T resolve(Class<T> type) {
		T instance = super.resolve(type);

		try {
			injectFields(instance);
		} catch (Exception e) {
			throw new RuntimeException("Field-Injection failed for " + type.getName(), e);
		}

		if (instance instanceof DIObject) {
			((DIObject) instance).resolve();
		}

		return instance;
	}

	public <T> T resolveDirect(Class<T> type) {
		T instance = super.resolveInstance(type);
		try {
			injectFields(instance);
		} catch (Exception e) {
			throw new RuntimeException("Field-Injection failed for " + type.getName(), e);
		}
		return instance;
	}

	private void injectFields(Object instance) throws IllegalAccessException {
		Class<?> clazz = instance.getClass();
		Field[] fields = clazz.getDeclaredFields();

		Class<?> superClazz = clazz;
		while ((superClazz = superClazz.getSuperclass()) != null && superClazz != Object.class) {
			injectFields_1(superClazz.getDeclaredFields(), instance);
		}

		injectFields_1(fields, instance);
	}

	private void injectFields_1(Field[] fields, Object instance) throws IllegalAccessException {
		for (Field field : fields) {
			if (field.isAnnotationPresent(DIInject.class)) {
				field.setAccessible(true);

				Object dependency = resolveDirect(field.getType());
				field.set(instance, dependency);
			}
		}
	}
}
