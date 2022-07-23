package de.theholyexception.holyapi.util.backuprecover;

import java.io.File;

public class BackupItem {
    private final BackupItemInfo itemInfo;
    private final File file;
    private BackupItem lastBackup;


    public static BackupItem create(File file, BackupManager manager) {
        return new BackupItem(file, manager.getBackupInfoResolver().apply(file.getName()));
    }
    private BackupItem(File file, BackupItemInfo itemInfo) {
        this.file = file;
        this.itemInfo = itemInfo;
    }

    public BackupType getBackupType() {
        return itemInfo.getType();
    }

    public File getFile() {
        return file;
    }

    public String getPath() {
        return itemInfo.getPath();
    }

    public long getTimeStamp() {
        return itemInfo.getTimeStamp();
    }

    public BackupItem getLastBackup() {
        return lastBackup;
    }
    public void setLastBackup(BackupItem lastBackup) {
        this.lastBackup = lastBackup;
    }

    public BackupItemInfo getItemInfo() {
        return itemInfo;
    }

    @Override
    public String toString() {
        return String.format("BackupItem:{File: %s; TimeStamp: %s, Type: %s, LastBackup: %s}", file.getName(), itemInfo.getTimeStamp(), itemInfo.getType(), (lastBackup == null ? "Null" : lastBackup.getBackupType()));
    }
}
