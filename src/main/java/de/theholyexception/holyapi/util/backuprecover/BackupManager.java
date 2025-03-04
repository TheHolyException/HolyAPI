package de.theholyexception.holyapi.util.backuprecover;

import de.theholyexception.holyapi.util.NotImplementedException;
import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;
import lombok.Getter;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BackupManager {

    private final Map<String, List<BackupItem>> items;
    private final File sourceFolder;
    private final File outputFolder;
    @Getter
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

    public static Map<String, List<BackupItemInfo>> scanSourceDirectory(File sourceFolder, Function<String, BackupItemInfo> resolver) {
        List<BackupItemInfo> tempItems = new ArrayList<>();
        for (File file : Objects.requireNonNull(sourceFolder.listFiles())) {
            BackupItemInfo item = resolver.apply(file.getName());
            if (!item.isValid()) continue;
            tempItems.add(item);
        }

        return tempItems.stream()
            .sorted(Comparator.comparing(BackupItemInfo::getTimeStamp))
            .collect(Collectors.groupingBy(BackupItemInfo::getPath));
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

    public File createFull(File folder) {
        throw new NotImplementedException();
    }

    public File createIncremental(File folder, BackupItem reference) {
        throw new NotImplementedException();
    }

    public File createDifferential(File folder, BackupItem reference) {
        throw new NotImplementedException();
    }


    public File restore(BackupItem item) {
        return restore(item.getPath(), item.getTimeStamp(), null);
    }

    public File restore(BackupItemInfo itemInfo) {
        return restore(itemInfo.getPath(), itemInfo.getTimeStamp(), null);
    }

    public File restore(String path, long timestamp, char[] password) {
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
                writeZipFile(zos, i, password, blackList);
                i = i.getLastBackup();
            } while (i != null);
        } catch (IOException ex) {
            LoggerProxy.log(LogLevel.ERROR, "Failed to restore backup", ex);
        }

        System.gc();
        System.runFinalization();
        return outputFile;
    }

    private void writeZipFile(ZipOutputStream zos, BackupItem item, char[] password, List<String> blacklist) {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(item.getFile().toPath()), password)) {

            LocalFileHeader localFileHeader;
            byte[] buffer = new byte[8192];
            int l;

            while((localFileHeader = zis.getNextEntry()) != null) {
                if (blacklist.contains(localFileHeader.getFileName())) continue;
                ZipParameters parameters = new ZipParameters();
                parameters.setFileNameInZip(localFileHeader.getFileName());
                parameters.setLastModifiedFileTime(localFileHeader.getLastModifiedTime());
                parameters.setFileComment("from " + item.getFile());
                zos.putNextEntry(parameters);
                while((l = zis.read(buffer)) != -1) {
                    zos.write(buffer, 0, l);
                }
                zos.closeEntry();
                blacklist.add(localFileHeader.getFileName());
            }



        } catch (IOException ex) {
            LoggerProxy.log(LogLevel.ERROR, "Failed to write zip file", ex);
        }
    }

    public static class Resolvers {

        private Resolvers() {}

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
                LoggerProxy.log(LogLevel.ERROR, "Failed to parse timestamp", ex);
                return null;
            }

            return new BackupItemInfo(segments[0], type, timeStamp);
        };

        public static final Function<String, BackupItemInfo> CORBINAN_EN = fileName -> {
            String[] segments = fileName.split(" ");
            if (segments.length < 4) return null;

            BackupType type;
            switch (segments[3].replace(".zip","").replace("(","").replace(")", "")) {
                case "Incremental":
                    type = BackupType.INCREMENTAL;
                    break;
                case "Full":
                    type = BackupType.FULL;
                    break;
                case "Differential":
                    type = BackupType.DIFFERENTIAL;
                    break;
                default: type = null;
            }

            long timeStamp = -1;
            try {
                timeStamp = CORBINAN_DE_SDF.parse(segments[1] + " " + segments[2]).getTime();
            } catch (ParseException ex) {
                LoggerProxy.log(LogLevel.ERROR, "Failed to parse timestamp", ex);
                return null;
            }

            return new BackupItemInfo(segments[0], type, timeStamp);
        };
    }

}
