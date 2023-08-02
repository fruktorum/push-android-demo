package com.devinotele.exampleapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        DevinoSdk.getInstance().requestLogs(getLogsCallback());
        DevinoSdk.getInstance().appStarted();
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
}