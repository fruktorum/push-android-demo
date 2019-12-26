package com.devinotele.exampleapp.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class Util {
    @SuppressWarnings("RegExpRedundantEscape")
    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    public static boolean checkEmail(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    void downloadFile(String url, Context context) {
        File file=new File(context.getExternalFilesDir(null),"sound.mp3");

        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse("https://notificationsounds.com/message-tones/to-the-point-568/download/mp3");

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("not_sound.mp3");
        request.setDescription("Downloading");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(false);
        request.setDestinationUri(Uri.fromFile(file));
        downloadmanager.enqueue(request);
    }

    public static final String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
