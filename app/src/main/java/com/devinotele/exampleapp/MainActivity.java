package com.devinotele.exampleapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.devinotele.devinosdk.sdk.DevinoLogsCallback;
import com.devinotele.devinosdk.sdk.DevinoSdk;
import com.devinotele.exampleapp.network.RetrofitHelper;
import com.devinotele.exampleapp.util.BriefTextWatcher;
import com.google.firebase.messaging.FirebaseMessaging;
import static com.devinotele.exampleapp.util.Util.checkEmail;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.Objects;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String SAVED_LOGS = "savedLogs";
    private String logs = "";
    private TextView logsView;
    private EditText email, phone;
    private SwitchCompat switchSound, switchPicture, switchDeeplink;
    private DevinoLogsCallback logsCallback;
    private Boolean logsVisible = false;
    private FrameLayout logsField;
    private ImageView logsIcon;
    private ScrollView logsScrollView;
    private RetrofitHelper retrofitHelper;
    private final int REQUEST_CODE_SEND_GEO = 11;
    private final int REQUEST_CODE_START_UPDATES = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setUpViews();
        showLogs(false);

        if(savedInstanceState != null) {
            logs = savedInstanceState.getString(SAVED_LOGS);
            logsView.setText(logs);
        }

        logsCallback = message -> runOnUiThread(
                () -> {
                    logs = logs + "\n" + message.replaceAll("\"", "\'") + "\n";
                    logsView.post(() -> logsView.setText(logs));
                    scrollDown(logsScrollView);
                }
        );

        retrofitHelper = new RetrofitHelper(logsCallback);

        DevinoSdk.getInstance().requestLogs(logsCallback);
        DevinoSdk.getInstance().appStarted();

        startGeo();

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean notificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        if(!notificationsEnabled) {
            logsCallback.onMessageLogged("Notifications are disabled for this application.");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString(SAVED_LOGS, logs);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DevinoSdk.getInstance().stop();
        Bundle state = new Bundle();
        state.putString(SAVED_LOGS, logs);
        onSaveInstanceState(state);
    }

    private void doRegistration() {
        email.setBackground(
                AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.ic_border_grey
                )
        );
        phone.setBackground(
                AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.ic_border_grey
                )
        );

        Boolean emailOk = checkEmail(email.getText().toString());
        Boolean phoneOk = PhoneNumberUtils.isGlobalPhoneNumber(phone.getText().toString()) &&
                phone.getText().length() == 12;

        if (!emailOk) email.setBackground(
                AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.ic_border_red)
        );
        if (!phoneOk) phone.setBackground(
                AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.ic_border_red)
        );

        if (phoneOk && emailOk)
            DevinoSdk.getInstance().register(phone.getText().toString(), email.getText().toString());
        else
            logsCallback.onMessageLogged("Invalid phone or email");
    }

    private void startGeo() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            DevinoSdk.getInstance().subscribeGeo(this, 1);
            logsCallback.onMessageLogged("Subscribed geo with interval: " + 1 + " min");
        } else {
            logsCallback.onMessageLogged("GEO PERMISSION MISSING!");
            DevinoSdk.getInstance().requestGeoPermission(this, REQUEST_CODE_START_UPDATES);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_update_user:
                doRegistration();
                break;

            case R.id.send_push:
                retrofitHelper.sendPushWithDevino(
                        FirebaseMessaging.getInstance(),
                        switchPicture.isChecked(),
                        switchSound.isChecked(),
                        switchDeeplink.isChecked()
                );
                break;

            case R.id.send_geo:
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    showGeoPermissionDialog();
                else
                    DevinoSdk.getInstance().sendCurrentGeo();
                break;

            case R.id.clear_logs:
                logs = "\n\n    ";
                logsView.setText(logs);
                break;

            case R.id.logs_toggle_button:
                showLogs(!logsVisible);
                break;
        }
    }

    private void showGeoPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Geo Permission Missing")
                .setMessage("May Devino SDK take care of that now?")
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                        DevinoSdk.getInstance().requestGeoPermission(this, REQUEST_CODE_SEND_GEO)
                )
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_START_UPDATES -> {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logsCallback.onMessageLogged("GEO PERMISSION GRANTED");
                    startGeo();
                } else {
                    logsCallback.onMessageLogged("PERMISSION DENIED");
                }
            }
            case REQUEST_CODE_SEND_GEO -> {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logsCallback.onMessageLogged("GEO PERMISSION GRANTED");
                    DevinoSdk.getInstance().sendCurrentGeo();
                } else {
                    logsCallback.onMessageLogged("PERMISSION DENIED");
                }
            }
        }
    }

    private void showLogs(Boolean show) {
        if (show) {
            logsField.setVisibility(View.VISIBLE);
            logsIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                            getApplicationContext(),
                            R.drawable.ic_arrow_drop_down
                    )
            );
        } else {
            logsField.setVisibility(View.GONE);
            logsIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                            getApplicationContext(),
                            R.drawable.ic_arrow_drop_up
                    )
            );
        }
        logsVisible = !logsVisible;
    }

    private void scrollDown(ScrollView scrollView) {
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void setUpViews() {
        logsView = findViewById(R.id.logs_view);
        TextView title = findViewById(R.id.title);
        logsScrollView = findViewById(R.id.logs_scroll_view);
        logsField = findViewById(R.id.logs_field);
        logsIcon = findViewById(R.id.logs_toggle_icon);
        email = findViewById(R.id.input_email);
        phone = findViewById(R.id.input_phone);
        switchSound = findViewById(R.id.switch_sound);
        switchPicture = findViewById(R.id.switch_picture);
        switchDeeplink = findViewById(R.id.switch_deeplink);

        ImageView clearLogs = findViewById(R.id.clear_logs);
        Button updateUser = findViewById(R.id.button_update_user);
        Button sendGeo = findViewById(R.id.send_geo);
        Button sendPush = findViewById(R.id.send_push);
        FrameLayout logsToggleButton = findViewById(R.id.logs_toggle_button);
        TextView version = findViewById(R.id.version_name);

        sendGeo.setOnClickListener(this);
        sendPush.setOnClickListener(this);
        clearLogs.setOnClickListener(this);
        updateUser.setOnClickListener(this);
        logsToggleButton.setOnClickListener(this);

        title.setOnLongClickListener(v -> {
            FirebaseMessaging firebaseInstanceId = FirebaseMessaging.getInstance();


            firebaseInstanceId.getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            try {
                                logsCallback.onMessageLogged("Firebase Error: " + Objects.requireNonNull(task.getException()).getMessage());
                            } catch (Throwable error) {
                                error.printStackTrace();
                                return;
                            }
                            return;
                        }
                        String token = task.getResult();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("token", token);
                        clipboard.setPrimaryClip(clip);

                        Toast.makeText(getApplicationContext()  , "token copied", Toast.LENGTH_SHORT).show();
                    });
            return false;
        });

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked) {
                    Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.push_sound);
                    DevinoSdk.getInstance().setCustomSound(sound);
                } else {
                    DevinoSdk.getInstance().useDefaultSound();
                }

        }
        );

        phone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) phone.setSelection(phone.getText().length());
        });

        phone.addTextChangedListener(new BriefTextWatcher() {
            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 3) phone.setText("+79");
                else if (!s.subSequence(0, 3).toString().equals("+79")) {
                    String prefix = s.subSequence(0, 3).toString();
                    String newValue = s.toString().replace(prefix, "+79");
                    phone.setText(newValue);
                }
                if (s.length() > 12) phone.setText(s.subSequence(0, 12));
                phone.setSelection(phone.getText().length());
            }
        });

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(getString(R.string.version_placeholder, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}