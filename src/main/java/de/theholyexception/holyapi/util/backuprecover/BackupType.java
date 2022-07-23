package de.theholyexception.holyapi.util.backuprecover;

public enum BackupType {

    INCREMENTAL,
    DIFFERENTIAL,
    FULL;

    public static BackupType of(String a) {
        switch (a) {
            case "Inkrementell": return INCREMENTAL;
            case "Vollständig": return FULL;
            case "Differentiell": return DIFFERENTIAL;
            default: return null;
        }
    }

}
