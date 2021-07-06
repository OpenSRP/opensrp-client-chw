package org.smartregister.chw.util;

import android.content.Context;
import android.os.Environment;

import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.GuideBooksFragmentContract;

import java.io.File;

import timber.log.Timber;

public class DownloadGuideBooksUtils extends DownloadUtil {

    public DownloadGuideBooksUtils(GuideBooksFragmentContract.DownloadListener downloadListener, String fileName, String directory, Context context) {
        this.fileName = fileName;
        folder = Environment.getExternalStorageDirectory() + File.separator +
                directory + File.separator +
                context.getResources().getConfiguration().locale + "/";
        this.downloadListener = downloadListener;
        this.serverUrl = getDownloadUrl(fileName, context);
    }

    public static String getDownloadUrl(String fileName, Context context) {
        return BuildConfig.guidebooks_url + context.getResources().getConfiguration().locale + "/" + fileName;
    }

    public void cancelDownload() {
        this.cancel(true);
        // delete the file in the device
        try {
            File file = new File(folder + fileName);
            if (file.exists())
                file.delete();
        } catch (Exception e) {
            Timber.v(e);
        }
    }
}