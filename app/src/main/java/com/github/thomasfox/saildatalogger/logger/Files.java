package com.github.thomasfox.saildatalogger.logger;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class Files {

    private static String TRACK_FILE_NAME_PREFIX = "track";
    private static String TRACK_FILE_NAME_SUFFIX = ".sailllog";

    private static final String TAG = "Files";

    public static File getStorageDir() {
        File file = new File(Environment.getExternalStorageDirectory(), "/saillogger");
        if (!file.mkdirs()) {
            Log.d(TAG, "Error creating file "  + file.getAbsolutePath());
        }
        return file;
    }

    public static File getTrackFile() {
        return new File(getStorageDir(), TRACK_FILE_NAME_PREFIX + getTrackFileNumber() + TRACK_FILE_NAME_SUFFIX);
    }

    private static Integer getTrackFileNumber() {
        File dir = getStorageDir();
        File[] files = dir.listFiles();
        int nextNumber = 1;
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(TRACK_FILE_NAME_PREFIX) && file.getName().endsWith(TRACK_FILE_NAME_SUFFIX)) {
                    String trackNumberString = file.getName().substring(
                            TRACK_FILE_NAME_PREFIX.length(), file.getName().length() - TRACK_FILE_NAME_SUFFIX.length());

                    Integer trackNumber;
                    try {
                        trackNumber = Integer.parseInt(trackNumberString);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (trackNumber >= nextNumber) {
                        nextNumber = trackNumber + 1;
                    }
                }
            }
        }
        return nextNumber;
    }
}
