package com.audio.stentor;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * AssetsUtils.
 */
public class AssetsUtils {
    public static void copyAssetsToSDCard(Context context, String sourceName, String strOutFileName) throws IOException {
        File file = new File(strOutFileName);
        if (file.exists()) return;

        File parent = file.getParentFile();
        if (!file.exists()) {
            parent.mkdir();
        }

        InputStream myInput = context.getAssets().open(sourceName);
        OutputStream myOutput = new FileOutputStream(strOutFileName);

        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
    }
}
