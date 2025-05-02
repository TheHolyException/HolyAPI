package de.theholyexception.holyapi.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SimpleDIContainer implements DIContainer {
	private Map<Class<?>, Object> instances = new HashMap<>();
	private Map<Class<?>, Class<?>> implementations = new HashMap<>();

	@Override
	public <T> void register(Class<T> type, T instance) {
		instances.put(type, instance);
	}

	@Override
	public <T> void register(Class<T> type, Class<? extends T> implementationClass) {
		implementations.put(type, implementationClass);
	}

	@Override
	public <T> T resolve(Class<T> type) {
		T instance = (T) instances.get(type);
		if (instance != null) {
			return instance;
		}

		Class<?> implementationClass = implementations.get(type);
		if (implementationClass == null) {
			implementationClass = type;
		}

		if (implementationClass.isInterface() || Modifier.isAbstract(implementationClass.getModifiers())) {
			throw new RuntimeException("Failed to create instance of " + implementationClass.getName());
		}

		try {
			Constructor<?>[] constructors = implementationClass.getConstructors();
			Arrays.sort(constructors, Comparator.comparing(Constructor::getParameterCount));

			Constructor<?> constructor = constructors[0];
			Object[] parameters = resolveConstructorParameters(constructor);

			instance = (T) constructor.newInstance(parameters);

			instances.put(type, instance);

			return instance;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create instance of " + implementationClass.getName(), e);
		}
	}

	private Object[] resolveConstructorParameters(Constructor<?> constructor) {
		Parameter[] parameters = constructor.getParameters();
		Object[] resolvedParameters = new Object[parameters.length];

		for (int i = 0; i < parameters.length; i++) {
			Class<?> parameterType = parameters[i].getType();
			resolvedParameters[i] = resolve(parameterType);
		}

		return resolvedParameters;
	}
}
