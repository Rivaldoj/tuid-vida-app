package com.autenticar.tuid.v2.utils;

import android.content.Context;
import android.net.Uri;

import com.autenticar.tuid.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.content.FileProvider;

public class FilesUtil {

    public static File createFile(Context context, String extension) throws IOException {
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + extension;
        return new File(context.getExternalFilesDir(null), fileName);
    }

    public static Uri getUriFromFile(Context context, File csvFile) {
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", csvFile);
    }
}
