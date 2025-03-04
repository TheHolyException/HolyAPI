package de.theholyexception.holyapi.util.logger;

import de.theholyexception.holyapi.util.ReflectionHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class LoggerProxy {

    private LoggerProxy() {}

    private static final boolean HAS_SLF_4_J;
    static {
        boolean result;
        try {
            Class.forName("org.slf4j.Logger");
            result = true;
        } catch (ClassNotFoundException e) {
            result = false;
        }
        HAS_SLF_4_J = result;
    }

    private static final Map<String, Logger> LOGGER_CACHE = Collections.synchronizedMap(new HashMap<>());
    private static Logger getLogger(String className) {
        Logger logger = LOGGER_CACHE.get(className);
        if (logger == null) {
            logger = Logger.getLogger(className);
            LOGGER_CACHE.put(className, logger);
        }
        return logger;
    }

    public static boolean isSLF4JEnabled() {
        return HAS_SLF_4_J;
    }

    private static Object getSLF4JLogger(String className) {
        if (!HAS_SLF_4_J) return null;
        try {
            return Class.forName("org.slf4j.LoggerFactory").getMethod("getLogger", String.class).invoke(null, className);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void log(LogLevel level, String message, String... params) {
        for (int i = 0; i < params.length; i++)
            message = message.replace("{" + i + "}", params[i]);

        String className = ReflectionHelper.getCallerClass(3);

        if (HAS_SLF_4_J) {
            try {
                Object logger = getSLF4JLogger(className);
                logger.getClass().getMethod(level.name().toLowerCase(), String.class).invoke(logger, message);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
            }
        } else {
            getLogger(className).log(level.getLevel(), message);
        }
    }

    public static void log(LogLevel level, String message) {
        String className = ReflectionHelper.getCallerClass(3);
        if (HAS_SLF_4_J) {
            try {
                Object logger = getSLF4JLogger(className);
                logger.getClass().getMethod(level.name().toLowerCase(), String.class).invoke(logger, message);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
            }
        } else {
            getLogger(className).log(level.getLevel(), message);
        }
    }

    public static void log(LogLevel level, String message, Throwable throwable) {
        String className = ReflectionHelper.getCallerClass(3);
        if (HAS_SLF_4_J) {
            try {
                Object logger = getSLF4JLogger(className);
                logger.getClass().getMethod(level.name().toLowerCase(), String.class, Throwable.class).invoke(logger, message, throwable);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
            }
        } else {
            getLogger(className).log(level.getLevel(), message);
        }
    }


}
