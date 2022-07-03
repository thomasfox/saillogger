package com.github.thomasfox.saildata.logger;

import android.app.Activity;
import android.util.Log;

import java.io.File;

public class Files {

    private static final String LOG_TAG = "saildata:Files";

    private static final String TRACK_FILE_NAME_PREFIX = "track";
    private static final String TRACK_FILE_NAME_SUFFIX = ".saildata";
    private static final String VIDEO_FILE_NAME_SUFFIX = ".mp4";

    private static File getStorageDir(Activity activity) {
        File file = activity.getExternalFilesDir(null);
        if  (file == null)
        {
            throw new RuntimeException("Cannot access externalFilesDir");
        }
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.w(LOG_TAG, "Error creating directory " + file.getAbsolutePath());
            }
        }
        return file;
    }

    public static File getTrackFile(int trackFileNumber, Activity activity) {
        return new File(
                getStorageDir(activity),
                TRACK_FILE_NAME_PREFIX + trackFileNumber + TRACK_FILE_NAME_SUFFIX);
    }

    public static File getVideoFile(int trackFileNumber, Activity activitiy) {
        return new File(
                getStorageDir(activitiy),
                TRACK_FILE_NAME_PREFIX + trackFileNumber + VIDEO_FILE_NAME_SUFFIX);
    }

    public static Integer getTrackFileNumber(Activity activitiy) {
        File dir = getStorageDir(activitiy);
        File[] files = dir.listFiles();
        int nextNumber = 1;
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(TRACK_FILE_NAME_PREFIX)
                        && file.getName().endsWith(TRACK_FILE_NAME_SUFFIX)) {
                    String trackNumberString = file.getName().substring(
                            TRACK_FILE_NAME_PREFIX.length(),
                            file.getName().length() - TRACK_FILE_NAME_SUFFIX.length());

                    int trackNumber;
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
