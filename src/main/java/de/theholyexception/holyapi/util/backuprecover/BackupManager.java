package de.theholyexception.holyapi.util.backuprecover;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private final Map<String, List<BackupItem>> items;
    private final File sourceFolder;
    private final File outputFolder;
    private final Function<String, BackupItemInfo> backupInfoResolver;


    public BackupManager(File sourceFolder, File outputFolder, Function<String, BackupItemInfo> backupInfoResolver) {
        Objects.requireNonNull(sourceFolder, "sourceFolder");
        Objects.requireNonNull(outputFolder, "outputFolder");
        Objects.requireNonNull(backupInfoResolver, "backupInfoResolver");

        this.items = new HashMap<>();
        this.sourceFolder = sourceFolder;
        this.outputFolder = outputFolder;
        this.backupInfoResolver = backupInfoResolver;

        if (!sourceFolder.exists()) throw new IllegalArgumentException("SourceFolder not exists");
        if (!outputFolder.exists()) outputFolder.mkdirs();
        scanSourceDirectory();
    }

    private void scanSourceDirectory() {
        // Converting all Files to BackupItem objects
        List<BackupItem> tempItems = new ArrayList<>();
        for (File file : Objects.requireNonNull(sourceFolder.listFiles())) {
            BackupItem item = BackupItem.create(file, this);
            if (item.getItemInfo() == null) continue;
            tempItems.add(item);
        }

        items.clear();
        // Group all BackupItems by their Path
        items.putAll(tempItems.stream()
                .sorted(Comparator.comparing(BackupItem::getTimeStamp))
                .collect(Collectors.groupingBy(BackupItem::getPath)));

        // Iterate over all Groups
        items.forEach((k, v) -> {

            BackupItem lastDiffOrFull = null;
            BackupItem lastItem = null;
            // Iterate over all BackupItems
            for (BackupItem bi : v) {
                if (bi.getBackupType().equals(BackupType.FULL)) {
                    lastDiffOrFull = bi;
                    lastItem = null;
                }

                if (lastDiffOrFull == null) continue;

                if (bi.getBackupType().equals(BackupType.DIFFERENTIAL)) {
                    bi.setLastBackup(lastDiffOrFull);
                    lastDiffOrFull = bi;
                    lastItem = null;
                }

                if (bi.getBackupType().equals(BackupType.INCREMENTAL)) {
                    bi.setLastBackup(lastItem == null ? lastDiffOrFull : lastItem);
                    lastItem = bi;
                }
            }
        });
    }

    public Function<String, BackupItemInfo> getBackupInfoResolver() {
        return backupInfoResolver;
    }

    public File createFull(File folder) {
        throw new NotImplementedException();
    }

    public File createIncremental(File folder, BackupItem reference) {
        throw new NotImplementedException();
    }

    public File createDifferential(File folder, BackupItem reference) {
        throw new NotImplementedException();
    }

    public File restore(String path, long timestamp) {
        List<BackupItem> localItems = items.get(path);
        if (localItems == null) throw new IllegalArgumentException("Invalid path");

        Optional<BackupItem> item  = localItems.stream()
                .filter(backupItem -> backupItem.getTimeStamp() <= timestamp)
                .max(Comparator.comparing(BackupItem::getTimeStamp));

        if (!item.isPresent()) throw new IllegalStateException("BackupItem not found!");

        BackupItemInfo itemInfo = item.get().getItemInfo();
        File outputFile = new File(outputFolder, itemInfo.getPath() + " " + itemInfo.getTimeStamp() + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
            BackupItem i = item.get();
            List<String> blackList = new ArrayList<>();

            do {
                writeZipFile(zos, i, blackList);
                i = i.getLastBackup();
            } while (i != null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.gc();
        System.runFinalization();
        return outputFile;
    }

    private void writeZipFile(ZipOutputStream zos, BackupItem item, List<String> blackList) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(item.getFile()))) {

            ZipEntry zisEntry;
            ZipEntry zosEntry;
            byte[] buffer = new byte[1024*1024*64];
            int l;

            while((zisEntry = zis.getNextEntry()) != null) {
                if (blackList.contains(zisEntry.getName())) continue;
                zosEntry = new ZipEntry(zisEntry.getName());
                zosEntry.setExtra(zisEntry.getExtra());
                zosEntry.setComment("from " + item.getFile());
                if (zisEntry.getCreationTime() != null) zosEntry.setCreationTime(zisEntry.getCreationTime());
                if (zisEntry.getLastAccessTime() != null) zosEntry.setLastAccessTime(zisEntry.getLastAccessTime());
                if (zisEntry.getLastModifiedTime() != null) zosEntry.setLastModifiedTime(zisEntry.getLastModifiedTime());
                zosEntry.setTime(zisEntry.getTime());
                zos.putNextEntry(zosEntry);

                while((l = zis.read(buffer)) != -1) {
                    zos.write(buffer, 0, l);
                }
                zos.closeEntry();
                blackList.add(zosEntry.getName());
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    //region Resolvers

    public static final SimpleDateFormat CORBINAN_DE_SDF = new SimpleDateFormat("yyyy-MM-dd HH;mm;ss");
    public static final Function<String, BackupItemInfo> CORBINAN_DE = fileName -> {
        String[] segments = fileName.split(" ");
        if (segments.length < 4) return null;

        BackupType type;
        switch (segments[3].replace(".zip","").replace("(","").replace(")", "")) {
            case "Inkrementell":
                type = BackupType.INCREMENTAL;
                break;
            case "Vollständig":
                type = BackupType.FULL;
                break;
            case "Differentiell":
                type = BackupType.DIFFERENTIAL;
                break;
            default: type = null;
        }

        long timeStamp = -1;
        try {
            timeStamp = CORBINAN_DE_SDF.parse(segments[1] + " " + segments[2]).getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }

        return new BackupItemInfo(segments[0], type, timeStamp);
    };

    //endregion Resolvers

}
