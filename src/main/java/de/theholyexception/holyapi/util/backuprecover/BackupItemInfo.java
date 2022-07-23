package de.theholyexception.holyapi.util.backuprecover;

public class BackupItemInfo {

    private final long timeStamp;
    private final String path;
    private final BackupType type;

    public BackupItemInfo(String path, BackupType type, long timeStamp) {
        this.timeStamp = timeStamp;
        this.path = path;
        this.type = type;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getPath() {
        return path;
    }

    public BackupType getType() {
        return type;
    }

    public boolean isValid() {
        return timeStamp != -1 && path != null && type != null;
    }
}
