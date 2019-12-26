package com.devinotele.devinosdk.sdk;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


class RequestGeoPermissionUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;

    RequestGeoPermissionUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run(Activity activity, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        requestCode);


        } else {
            logsCallback.onMessageLogged("Permission has already been granted");
        }

    }

}
