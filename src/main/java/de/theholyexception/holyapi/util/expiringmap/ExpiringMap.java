package de.theholyexception.holyapi.util.expiringmap;

import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExpiringMap<K,V> extends ConcurrentHashMap<K, V> {

    private static final Thread thread;
    private static final List<List<ExpiringListener>> listList;

    static {
        listList = new ArrayList<>();
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                long currentTime = System.currentTimeMillis();
                listList.forEach(list -> {
                    List<ExpiringListener> removed = new ArrayList<>();
                    for (ExpiringListener listener : list) {
                        if (currentTime >= listener.getTimeStamp()) {
                            listener.expire();
                            removed.add(listener);
                        }
                    }
                    list.removeAll(removed);
                });
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    LoggerProxy.log(LogLevel.ERROR, "InterruptedException", ex);
                }
            }
        }, "ExpiringMap-Cleanup");
        thread.setDaemon(true);
        thread.start();
    }

    private final transient List<ExpiringListener> expiringListenerList;
    private final long holdTime;
    private final boolean includeAccess;

    /**
     *
     * @param millis time to hold an entry
     * @param includeAccess reset the timer on accessing the entry
     */
    public ExpiringMap(long millis, boolean includeAccess) {
        this.holdTime = millis;
        this.includeAccess = includeAccess;
        expiringListenerList = new ArrayList<>();
        listList.add(expiringListenerList);
    }

    private void put0(K key, V value) {
        expiringListenerList.add(new ExpiringListener(System.currentTimeMillis()+holdTime, key, value) {
            @Override
            public void expire() {
                ExpiringMap.super.remove(key, value);
            }
        });
    }

    private void get0(K key, V value) {
        ExpiringListener  listener = getListener(key, value);
        if (listener == null) return;
        listener.setTimeStamp(System.currentTimeMillis()+holdTime);
    }

    private ExpiringListener getListener(K key, V value) {
        for (ExpiringListener kvExpiringListener : expiringListenerList) {
            if (kvExpiringListener.getKey().equals(key) && kvExpiringListener.getValue().equals(value))
                return kvExpiringListener;
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        put0(key, value);
        return super.put(key, value);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        put0(key, value);
        return super.putIfAbsent(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put0);
        super.putAll(m);
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (includeAccess) get0((K) key, value);
        return value;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V value = super.getOrDefault(key, defaultValue);
        if (includeAccess) get0((K) key, value);
        return value;
    }
}
