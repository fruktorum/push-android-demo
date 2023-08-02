package com.devinotele.exampleapp;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.CLIPBOARD_SERVICE;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.devinotele.devinosdk.sdk.DevinoLogsCallback;
import com.devinotele.devinosdk.sdk.DevinoSdk;
import com.devinotele.exampleapp.network.RetrofitHelper;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.ReplaySubject;

public class HomeFragment extends Fragment implements View.OnClickListener {

    boolean isRegisteredUser;
    ReplaySubject<String> logsRx;
    private TextView logsView;
    private String logsLocal;
    private DevinoLogsCallback logsCallback;
    private MainActivityCallback mainActivityCallback;
    private ScrollView logsScrollView;
    private static final String SAVED_LOGS = "savedLogs";
    private Button removeRegistration, registration;
    private RetrofitHelper retrofitHelper;
    private final int REQUEST_CODE_SEND_GEO = 11;
    private final int REQUEST_CODE_START_UPDATES = 13;
    private SwitchCompat switchSound, switchPicture, switchDeeplink;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRegisteredUser = HomeFragmentArgs.fromBundle(getArguments()).getIsRegisteredUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof MainActivityCallback)) {
            throw new ClassCastException(getString(R.string.class_cast_exception));
        }
        mainActivityCallback = (MainActivityCallback) context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        logsCallback = mainActivityCallback.getLogsCallback();
        logsRx = mainActivityCallback.getLogs();
        retrofitHelper = new RetrofitHelper(logsCallback);

        setUpViews();

        if (savedInstanceState != null) {
            logsLocal = savedInstanceState.getString(SAVED_LOGS);
            logsView.setText(logsLocal);
        }
        logsRx.subscribe(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(String log) {
                String logs = logsView.getText().toString() + log;
                logsView.setText(logs);
                scrollDown(logsScrollView);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }
        });

        startGeo();
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean notificationsEnabled = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();
        if (!notificationsEnabled) {
            logsCallback.onMessageLogged(getString(R.string.notifications_disabled));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVED_LOGS, logsLocal);
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onDestroy() {
        Bundle state = new Bundle();
        state.putString(SAVED_LOGS, logsLocal);
        logsRx.unsubscribeOn(mainThread());
        onSaveInstanceState(state);
        super.onDestroy();
    }

    private void setUpViews() {
        logsView = requireView().findViewById(R.id.logs_view);
        TextView title = requireView().findViewById(R.id.title);
        logsScrollView = requireView().findViewById(R.id.logs_scroll_view);
        ImageView clearLogs = requireView().findViewById(R.id.clear_logs);
        TextView version = requireView().findViewById(R.id.version_name);

        switchSound = requireView().findViewById(R.id.switch_sound);
        switchPicture = requireView().findViewById(R.id.switch_picture);
        switchDeeplink = requireView().findViewById(R.id.switch_deeplink);

        Button sendGeo = requireView().findViewById(R.id.send_geo);
        Button sendPush = requireView().findViewById(R.id.send_push);
        Button registration = requireView().findViewById(R.id.btn_registration);

        sendGeo.setOnClickListener(this);
        sendPush.setOnClickListener(this);
        registration.setOnClickListener(this);
        clearLogs.setOnClickListener(this);

        if (isRegisteredUser) {
            registration.setVisibility(View.GONE);
        } else {
            registration.setVisibility(View.VISIBLE);
        }

        title.setOnLongClickListener(v -> {
            FirebaseMessaging firebaseInstanceId = FirebaseMessaging.getInstance();

            firebaseInstanceId.getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            try {
                                logsCallback.onMessageLogged(
                                        getString(
                                                R.string.error_firebase
                                        ) + Objects.requireNonNull(
                                                task.getException()
                                        ).getMessage()
                                );
                            } catch (Throwable error) {
                                error.printStackTrace();
                                return;
                            }
                            return;
                        }
                        String token = task.getResult();
                        ClipboardManager clipboard =
                                (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("token", token);
                        clipboard.setPrimaryClip(clip);

                        Toast.makeText(
                                requireContext(),
                                "token copied",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
            return false;
        });

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                                + "://"
                                + requireContext().getPackageName()
                                + "/" + R.raw.push_sound
                        );
                        DevinoSdk.getInstance().setCustomSound(sound);
                    } else {
                        DevinoSdk.getInstance().useDefaultSound();
                    }

                }
        );

        try {
            PackageInfo pInfo = requireContext()
                    .getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            version.setText(getString(R.string.version_placeholder, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        if (v.getId() == R.id.btn_registration) {
            if (!isRegisteredUser) {
                navController.navigate(R.id.registrationFragment);
            }
        }

        if (v.getId() == R.id.clear_logs) {
            logsLocal = "\n";
            logsView.setText(logsLocal);
        }

        if (v.getId() == R.id.send_push) {
            try {
                retrofitHelper.sendPushWithDevino(
                        FirebaseMessaging.getInstance(),
                        switchPicture.isChecked(),
                        switchSound.isChecked(),
                        switchDeeplink.isChecked()
                );
            } catch (Exception e) {
                Log.d(
                        getString(R.string.logs_tag),
                        getString(R.string.error_send_push) + " " + e.getMessage()
                );
            }
        }

        if (v.getId() == R.id.send_geo) {
            try {
                if (ActivityCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    showGeoPermissionDialog();
                } else
                    DevinoSdk.getInstance().sendCurrentGeo();
            } catch (Exception e) {
                Log.d(
                        getString(R.string.logs_tag),
                        getString(R.string.error_send_geo) + " " + e.getMessage()
                );
            }
        }
    }

    private void removeUpdateRegistrationUi() {
        isRegisteredUser = false;
        removeRegistration.setVisibility(View.GONE);
        registration.setText(getString(R.string.registration));
    }

    private void scrollDown(ScrollView scrollView) {
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void startGeo() {
        if (ActivityCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            DevinoSdk.getInstance().subscribeGeo(requireContext(), 1);
            logsCallback.onMessageLogged(getString(R.string.subscribed_geo_interval) + 1 + getString(R.string.min));
        } else {
            logsCallback.onMessageLogged(getString(R.string.geo_permission_missing));
            DevinoSdk.getInstance().requestGeoPermission(requireActivity(), REQUEST_CODE_START_UPDATES);
        }
    }

    private void showGeoPermissionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Geo Permission Missing")
                .setMessage("May Devino SDK take care of that now?")
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                        DevinoSdk.getInstance().requestGeoPermission(requireActivity(), REQUEST_CODE_SEND_GEO)
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

}
