package de.theholyexception.holyapi.di;

public interface DIContainer {
	<T> void register(Class<T> type, T instance);
	@Deprecated
	default <T> void register(Class<T> type, Class<? extends T> implementationClass) {}
	<T> T resolve(Class<T> type);
}
