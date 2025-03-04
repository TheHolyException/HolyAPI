package de.theholyexception.holyapi.util.logger;

import lombok.Getter;

import java.util.logging.Level;

public enum LogLevel {

    DEBUG(Level.FINE),
    INFO(Level.INFO),
    WARN(Level.WARNING),
    ERROR(Level.SEVERE);

    @Getter
    private final Level level;

    LogLevel(Level level) {
        this.level = level;
    }

}
