package de.theholyexception.holyapi.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;

public class ComplexDIContainer implements DIContainer {

	private Map<Class<?>, Class<?>> implementations = new HashMap<>();
	private final Map<Class<?>, Object> instances = new HashMap<>();
	private final Map<Class<?>, List<FutureDependency>> futureDependencies = new HashMap<>();

	private boolean resolveCircularDependencies = false;
	private boolean constructorInjection = false;
	private boolean fieldInjection = true;
	private boolean constructDependencies = false;

	public ComplexDIContainer setResolveCircularDependencies(boolean value) {
		this.resolveCircularDependencies = value;
		return this;
	}

	public ComplexDIContainer setConstructorInjection(boolean value) {
		this.constructorInjection = value;
		return this;
	}

	public ComplexDIContainer setFieldInjection(boolean value) {
		this.fieldInjection = value;
		return this;
	}

	public ComplexDIContainer setConstructDependencies(boolean value) {
		this.constructDependencies = value;
		return this;
	}

	public <T> void register(Class<T> type) {
		implementations.put(type, type);
	}

	public <T> void register(Class<T> type, Class<? extends T> implementationClass) {
		implementations.put(type, implementationClass);
	}

	public <T> void register(T instance) {
		instances.put(instance.getClass(), instance);
	}

	@Override
	public <T> void register(Class<T> type, T instance) {
		instances.put(type, instance);
	}

	public <T> T resolve(Class<T> type) {
		if (instances.containsKey(type))
			return (T) instances.get(type);

		// Construct instance
		Constructor<?>[] constructors = type.getDeclaredConstructors();

		if (constructors.length == 0)
			throw new DependencyInjectionException("No constructors found for " + type.getName());

		T instance;
		Constructor<?> constructor = constructors[0];
		try {
			Object[] parameters = null;
			if (constructorInjection) {
				parameters = resolveConstructorParameters(constructor);
			} else {
				parameters = new Object[constructor.getParameterCount()];
				for (int i = 0; i < constructor.getParameterCount(); i++)
					parameters[i] = null;
			}

			// enforce the accessibility of the constructor for private constructors
			if (!constructor.isAccessible())
				constructor.setAccessible(true);

			instance = (T) constructor.newInstance(parameters);
		} catch (Exception e) {
			throw new DependencyInjectionException("Failed to create instance of " + type.getName(), e);
		}

		instances.put(type, instance);
		if (fieldInjection)
			injectFields(instance);

		// Check if the instance is needed in some future dependency
		if (resolveCircularDependencies)
			resolveCircularDependencies(instance, type);

		return instance;
	}

	private void resolveCircularDependencies(Object instance, Class<?> type) {
		List<FutureDependency> dependencies = this.futureDependencies.get(type);
		if (dependencies != null) {
			for (FutureDependency dependency : dependencies) {
				try {
					Field field = dependency.getField();
					field.setAccessible(true);
					field.set(dependency.getInstance(), instance);
				} catch (ReflectiveOperationException ex) {
					throw new DependencyInjectionException("Failed to inject FutureDependency " +
						"of " + type.getName() + " " +
						"into " + dependency.getInstance().getClass().getName() + "." + dependency.getField().getName(), ex);
				}
			}
		}
		futureDependencies.remove(type);
	}

	public void injectFields(Object instance) {
		Class<?> clazz = instance.getClass();
		Field[] fields = clazz.getDeclaredFields();

		Class<?> superClazz = clazz;
		while ((superClazz = superClazz.getSuperclass()) != null && superClazz != Object.class) {
			injectFields(superClazz.getDeclaredFields(), instance);
		}

		injectFields(fields, instance);
	}

	private void injectFields(Field[] fields, Object instance) {
		for (Field field : fields) {
			if (field.isAnnotationPresent(DIInject.class)) {
				Object dependency = resolveDependency(field.getType());

				// If the dependency exists, inject it
				// If not, add it to futureDependencies and set it later when the dependency is created
				if (dependency != null) {
					try {
						field.setAccessible(true);
						field.set(instance, dependency);
					} catch (Exception e) {
						throw new DependencyInjectionException("Failed to inject field " + field.getName() + " of " + instance.getClass().getName(), e);
					}
				} else if (resolveCircularDependencies) {
					futureDependencies.computeIfAbsent(field.getType(),
						k -> new ArrayList<>()).add(new FutureDependency(instance, field));
				}
			}
		}
	}

	private Object resolveDependency(Class<?> type) {
		Object dependency = instances.get(type);
		if (dependency == null && constructDependencies && implementations.containsKey(type)) {
			dependency = resolve(type);
		}
		return dependency;
	}

	private Object[] resolveConstructorParameters(Constructor<?> constructor) {
		Parameter[] parameters = constructor.getParameters();
		Object[] resolvedParameters = new Object[parameters.length];

		for (int i = 0; i < parameters.length; i++) {
			Class<?> parameterType = parameters[i].getType();
			Object dependency = resolveDependency(parameterType);
			resolvedParameters[i] = dependency;
			if (dependency == null) {
				throw new DependencyInjectionException(
					"Failed to resolve parameter " + parameterType.getName() +
					" for class " + constructor.getDeclaringClass().getName());
			}
		}

		return resolvedParameters;
	}
}
