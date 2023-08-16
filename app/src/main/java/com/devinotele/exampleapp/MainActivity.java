package com.devinotele.exampleapp;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.devinotele.devinosdk.sdk.DevinoLogsCallback;
import com.devinotele.devinosdk.sdk.DevinoSdk;
import io.reactivex.subjects.ReplaySubject;

public class MainActivity extends AppCompatActivity implements MainActivityCallback {

    NavController navController;
    NavHostFragment navHostFragment;
    private DevinoLogsCallback logsCallback;
    public String logs = "";
    public ReplaySubject<String> logsRx = ReplaySubject.create();
    private final int REQUEST_CODE_START_UPDATES = 13;
    private final int REQUEST_CODE_NOTIFICATION = 14;
    private final int REQUEST_CODE_NOTIFICATION_AND_GEO = 15;
    private final int REQUEST_CODE_SEND_GEO = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        DevinoSdk.getInstance().requestLogs(getLogsCallback());
        DevinoSdk.getInstance().appStarted();
        DevinoSdk.getInstance().activateSubscription(true);

        checkPermission();

        if (savedInstanceState == null) {
            Log.d(getString(R.string.tag), "intent.data = " + getIntent().getData());
            if (getIntent().getData() != null) {
                if (getIntent().getData().toString()
                        .equals(getString(R.string.devino_default_deeplink))
                ) {
                    navController.navigate(R.id.homeFragment);
                }
            }
        }
    }

    private void createLogsCallback() {
        logsCallback = message -> runOnUiThread(
                () -> {
                    logs = "\n" + message.replaceAll("\"", "\'") + "\n";
                    logsRx.onNext(logs);
                    Log.d(getString(R.string.logs_tag), logs);
                }
        );
    }

    @Override
    protected void onDestroy() {
        DevinoSdk.getInstance().unsubscribeLogs();
        DevinoSdk.getInstance().stop();
        super.onDestroy();
    }

    @Override
    public DevinoLogsCallback getLogsCallback() {
        if (logsCallback == null) {
            createLogsCallback();
        }
        return logsCallback;
    }

    @Override
    public ReplaySubject<String> getLogs() {
        return logsRx;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_START_UPDATES, REQUEST_CODE_SEND_GEO -> {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logsCallback.onMessageLogged(getString(R.string.geo_permission_granted));
                    startGeo();
                    DevinoSdk.getInstance().sendCurrentGeo();
                } else {
                    logsCallback.onMessageLogged(getString(R.string.geo_permission_missing));
                }
            }
            case REQUEST_CODE_NOTIFICATION_AND_GEO -> {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    logsCallback.onMessageLogged(getString(R.string.geo_permission_granted));
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_granted));
                    startGeo();
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED
                        && grantResults[1] == PackageManager.PERMISSION_DENIED
                ) {
                    logsCallback.onMessageLogged(getString(R.string.geo_permission_missing));
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_missing));
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_DENIED
                ) {
                    logsCallback.onMessageLogged(getString(R.string.geo_permission_granted));
                    startGeo();
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_missing));
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    logsCallback.onMessageLogged(getString(R.string.geo_permission_missing));
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_granted));
                }
            }
            case REQUEST_CODE_NOTIFICATION -> {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_granted));
                } else {
                    logsCallback.onMessageLogged(getString(R.string.notification_permission_missing));
                }
            }

        }
    }

    private void startGeo() {
        DevinoSdk.getInstance().subscribeGeo(this, 1);
        logsCallback.onMessageLogged(
                getString(R.string.subscribed_geo_interval, 1, getString(R.string.min))
        );
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            DevinoSdk.getInstance().requestGeoAndNotificationPermissions(
                    this,
                    REQUEST_CODE_NOTIFICATION_AND_GEO
            );
        } else {
            DevinoSdk.getInstance().requestGeoPermission(this, REQUEST_CODE_START_UPDATES);
        }
    }
}