package de.theholyexception.holyapi.di;

import de.theholyexception.holyapi.util.logger.LoggerProxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Deprecated
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

	public <T> T resolveInstance(Class<T> type) {
		if (instances.containsKey(type))
			return (T) instances.get(type);
		System.out.println("" + type.getName() + " not found");
		return null;
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
			throw new RuntimeException("Failed to create instance of abstract or interface class : " + implementationClass.getName());
		}

		try {
			Constructor<?>[] constructors = implementationClass.getConstructors();
			Arrays.sort(constructors, Comparator.comparing(Constructor::getParameterCount));

			if (constructors.length == 0) {
				throw new RuntimeException("No constructors found for " + implementationClass.getName());
			}

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

	public Map<Class<?>, Object> getInstances() {
		return instances;
	}

	public Map<Class<?>, Class<?>> getImplementations() {
		return implementations;
	}
}
