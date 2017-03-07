package com.serp1983.nokiacomposer.logic;

import java.io.File;
import java.io.RandomAccessFile;

class RingtoneSaver {

    static String makeRingtoneFilename(CharSequence title, String extension, int fileKind) {
        String parentDir;
        switch(fileKind) {
            default:
            case SetAsRingtoneService.FILE_KIND_ALARM:
                parentDir = "/sdcard/media/audio/alarms";
                break;
            case SetAsRingtoneService.FILE_KIND_NOTIFICATION:
                parentDir = "/sdcard/media/audio/notifications";
                break;
            case SetAsRingtoneService.FILE_KIND_RINGTONE:
                parentDir = "/sdcard/media/audio/ringtones";
                break;
        }

        // Create the parent directory
        File parentDirFile = new File(parentDir);
        parentDirFile.mkdirs();

        // If we can't write to that special path, try just writing
        // directly to the sdcard
        if (!parentDirFile.isDirectory()) {
            parentDir = "/sdcard";
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
        for (int i = 0; i < 300; i++) {
            String testPath;
            if (i > 0)
                testPath = parentDir + "/" + filename + i + extension;
            else
                testPath = parentDir + "/" + filename + extension;

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
