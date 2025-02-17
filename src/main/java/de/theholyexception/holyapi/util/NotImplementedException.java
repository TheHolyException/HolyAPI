package de.theholyexception.holyapi.util;

public class NotImplementedException extends RuntimeException {
    private static final long serialVersionUID = 78163827546872365L;

    public NotImplementedException() {}
    public NotImplementedException(String message) { super(message); }
}
