package com.bhaptics.common;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by westside on 2016-04-19.
 */
public class FileUtils {
    public static final String TAG = FileUtils.class.getSimpleName();

    public static void writeToSDFile(String fileName, byte[] file_content){
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        dir.mkdirs();
        File file = new File(dir, fileName);

        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(file_content);
            f.flush();
            f.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException : ", e);
        }
    }

    public static String removeExtention(String filePath) {
        File f = new File(filePath);

        if (f.isDirectory()) return filePath;

        String name = f.getName();

        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0)
        {
            return filePath;
        }
        else
        {
            File renamed = new File(f.getParent(), name.substring(0, lastPeriodPos));
            return renamed.getPath();
        }
    }

}
