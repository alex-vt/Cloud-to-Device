package com.oleksiykovtun.cloudtodevice.android;

import com.dropbox.client2.DropboxAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Files
 */
public class Files {

    public final static String PATTERNS_DELIMITER_REGEX = "( (?=[/*]))";

    private final static String DATE_DELIMITER = ".";
    private final static String TIME_DELIMITER = ":";
    private final static List<String> MONTHS = Arrays.asList(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    public static String getExtension(String filePath) {
        // todo string to constant
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        return (fileName.lastIndexOf(".") < 0) ? "" : fileName.substring(fileName.lastIndexOf("."));
    }

    public static boolean fileExists(String containingFolderPath, FileEntry cloudFileEntry) {
        return createFile(containingFolderPath, getLocalPath(cloudFileEntry)).exists();
    }

    public static String getTimestamp(String timestamp) {
        String day = timestamp.substring(5, 7);
        // todo string to constant
        String month = String.format("%02d", MONTHS.indexOf(timestamp.substring(8, 11)) + 1);
        String year = timestamp.substring(12, 16);
        String hour = timestamp.substring(17, 19);
        String minute = timestamp.substring(20, 22);
        String second = timestamp.substring(23, 25);
        // todo string to constant
        return year + DATE_DELIMITER + month + DATE_DELIMITER + day + " "
                + hour + TIME_DELIMITER + minute + TIME_DELIMITER + second;
    }

    public static String getLocalPath(FileEntry cloudFileEntry) {
        // todo string to constant
        String path = cloudFileEntry.getPath().substring(1);
        String timestamp = ".[" + getTimestamp(cloudFileEntry.getModified());
        String revision = " rev. " + cloudFileEntry.getRevision() + "]";
        String extension = getExtension(cloudFileEntry.getPath());
        return path + timestamp + revision + extension;
    }

    private static FileOutputStream outputStream;

    public static void writeEntryToFile(DropboxAPI cloudApi, FileEntry fileEntry,
                                        String folder, String path) throws Exception {
        // todo check security and string to constant
        File tempFile = createFile(folder, "temp" + Preferences.APP_KEY);
        outputStream = null;
        outputStream = new FileOutputStream(tempFile);
        cloudApi.getFile(fileEntry.getPath(), null, outputStream, null);
        outputStream.flush();
        outputStream.close();
        if (! tempFile.renameTo(createFile(folder, path))) {
            throw new IOException("File cannot be moved to:\n" + folder + path);
        }
    }

    private static boolean pathMatchesAnyPattern(String path, List<String> rawPathPatternList) {
        final String rawPathFixedPartRegex =  "[^*]+";
        final String rawPathArbitraryPartRegex =  "[*]+";
        final String pathFixedPartRegex =  "\\\\Q$0\\\\E";
        final String pathArbitraryPartRegex =  ".+";
        boolean result = false;
        for (String rawPathPattern : rawPathPatternList) {
            String pathPatternRegex = rawPathPattern.trim()
                    .replaceAll(rawPathFixedPartRegex, pathFixedPartRegex)
                    .replaceAll(rawPathArbitraryPartRegex, pathArbitraryPartRegex);
            if (path.matches(pathPatternRegex)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static boolean isEntryIncluded(DropboxAPI.DeltaEntry deltaEntry,
                                    List<String> excludedPatterns) {
        return deltaEntry.metadata != null
                && ! ((DropboxAPI.Entry)deltaEntry.metadata).isDir
                && ! ((DropboxAPI.Entry)deltaEntry.metadata).isDeleted
                && ! pathMatchesAnyPattern(deltaEntry.lcPath, excludedPatterns);
    }

    public static Map.Entry<FileEntry[], String> getCloudChanges(DropboxAPI cloudApi, String cursor,
                    List<String> excludedPatterns) throws Exception {
        Set<FileEntry> changedEntrySet = new TreeSet<>();
        DropboxAPI.DeltaPage<DropboxAPI.Entry> deltaPage;
        do {
            deltaPage = cloudApi.delta(cursor);
            cursor = deltaPage.cursor;
            for (DropboxAPI.DeltaEntry deltaEntry : deltaPage.entries) {
                if (isEntryIncluded(deltaEntry, excludedPatterns)) {
                    changedEntrySet.add(new FileEntry(deltaEntry.lcPath,
                            ((DropboxAPI.Entry)deltaEntry.metadata).modified,
                            ((DropboxAPI.Entry)deltaEntry.metadata).rev));
                }
            }
        } while (deltaPage.hasMore);
        return new AbstractMap.SimpleEntry<>(
                changedEntrySet.toArray(new FileEntry[changedEntrySet.size()]), cursor);
    }

    public static File createFile(String folder, String path) {
        File file = new File(folder, path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

}
