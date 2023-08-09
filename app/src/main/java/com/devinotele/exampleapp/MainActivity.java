package com.devinotele.exampleapp;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
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

        startGeo();

        Log.d("DevinoPush", "intent = " + getIntent());

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
    protected void onResume() {
        super.onResume();
        boolean notificationsEnabled =
                NotificationManagerCompat.from(this).areNotificationsEnabled();
        if (!notificationsEnabled) {
            logsCallback.onMessageLogged(getString(R.string.notifications_disabled));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Integer REQUEST_CODE_NOTIFICATION = 14;
                DevinoSdk.getInstance().requestNotificationPermission(
                        this,
                        REQUEST_CODE_NOTIFICATION
                );
            }
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        final int REQUEST_CODE_SEND_GEO = 11;
        switch (requestCode) {
            case REQUEST_CODE_START_UPDATES -> {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logsCallback.onMessageLogged(getString(R.string.geo_permission_granted));
                    startGeo();
                } else {
                    logsCallback.onMessageLogged(getString(R.string.permission_denied));
                }
            }
            case REQUEST_CODE_SEND_GEO -> {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logsCallback.onMessageLogged(getString(R.string.geo_permission_granted));
                    DevinoSdk.getInstance().sendCurrentGeo();
                } else {
                    logsCallback.onMessageLogged(getString(R.string.permission_denied));
                }
            }
        }
    }

    private void startGeo() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            DevinoSdk.getInstance().subscribeGeo(this, 1);
            logsCallback.onMessageLogged(getString(R.string.subscribed_geo_interval) + 1 + getString(R.string.min));
        } else {
            logsCallback.onMessageLogged(getString(R.string.geo_permission_missing));
            DevinoSdk.getInstance().requestGeoPermission(this, REQUEST_CODE_START_UPDATES);
        }
    }
}