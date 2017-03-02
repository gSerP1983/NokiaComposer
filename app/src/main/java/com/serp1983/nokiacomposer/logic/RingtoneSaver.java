package com.serp1983.nokiacomposer.logic;

import java.io.File;
import java.io.RandomAccessFile;
import static com.serp1983.nokiacomposer.FileSaveDialog.*;

public class RingtoneSaver {

    public static String makeRingtoneFilename(CharSequence title, String extension, int fileKind) {
        String parentdir;
        switch(fileKind) {
            default:
            case FILE_KIND_MUSIC:
                parentdir = "/sdcard/media/audio/music";
                break;
            case FILE_KIND_ALARM:
                parentdir = "/sdcard/media/audio/alarms";
                break;
            case FILE_KIND_NOTIFICATION:
                parentdir = "/sdcard/media/audio/notifications";
                break;
            case FILE_KIND_RINGTONE:
                parentdir = "/sdcard/media/audio/ringtones";
                break;
        }

        // Create the parent directory
        File parentDirFile = new File(parentdir);
        parentDirFile.mkdirs();

        // If we can't write to that special path, try just writing
        // directly to the sdcard
        if (!parentDirFile.isDirectory()) {
            parentdir = "/sdcard";
        }

        // Turn the title into a filename
        String filename = "";
        for (int i = 0; i < title.length(); i++) {
            if (Character.isLetterOrDigit(title.charAt(i))) {
                filename += title.charAt(i);
            }
        }

        // Try to make the filename unique
        String path = null;
        for (int i = 0; i < 100; i++) {
            String testPath;
            if (i > 0)
                testPath = parentdir + "/" + filename + i + extension;
            else
                testPath = parentdir + "/" + filename + extension;

            try {
                new RandomAccessFile(new File(testPath), "r");
            } catch (Exception e) {
                // Good, the file didn't exist
                path = testPath;
                break;
            }
        }

        return path;
    }
}
