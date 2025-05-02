package de.theholyexception.holyapi.di;

import java.lang.reflect.Field;

public class AdvancedDIContainer extends SimpleDIContainer {
	@Override
	public <T> T resolve(Class<T> type) {
		T instance = super.resolve(type);

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

		for (Field field : fields) {
			if (field.isAnnotationPresent(Inject.class)) {
				field.setAccessible(true);

				Object dependency = resolve(field.getType());
				field.set(instance, dependency);
			}
		}
	}
}
