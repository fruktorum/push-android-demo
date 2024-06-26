package com.devinotele.exampleapp.network;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.devinotele.devinosdk.sdk.DevinoLogsCallback;
import com.devinotele.devinosdk.sdk.DevinoSdk;
import com.devinotele.exampleapp.BuildConfig;
import com.devinotele.exampleapp.R;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RetrofitHelper {

    private final DevinoPushApi devinoPushApi;
    private final DevinoLogsCallback callback;
    private final RetrofitClientInstance retrofitClientInstance;

    public RetrofitHelper(DevinoLogsCallback callback) {
        devinoPushApi = RetrofitClientInstance
                .getRetrofitInstanceForDevinoPush()
                .create(DevinoPushApi.class);
        this.callback = callback;
        retrofitClientInstance = new RetrofitClientInstance();
    }

    @SuppressLint("CheckResult")
    public void sendPushWithDevino(
            FirebaseMessaging firebaseInstanceId,
            Boolean isPicture,
            Boolean isSound,
            Boolean isDeepLink,
            Boolean isAction,
            Context context
    ) {
        firebaseInstanceId.getToken().addOnCompleteListener(task -> {
            Log.d("Firebase", " " + task.isSuccessful());
            if (!task.isSuccessful()) {
                return;
            }
            String token = task.getResult();
            String message = "Simple push";

            HashMap<String, Object> body = new HashMap<>();

            body.put("from", BuildConfig.DEVINO_APP_ID);
            body.put("validity", 3600);
            body.put("to", token);
            body.put("title", "Devino Demo");
            body.put("badge", 0);
            body.put("priority", "HIGH");
            body.put("silentPush", false);

                    HashMap<String, Object> customData = new HashMap<>();
                    customData.put("login", "loginValue");
                    body.put("customData", customData);

                    HashMap<String, Object> android = new HashMap<>();

                    if (isAction) {
                        android.put("action", "devino://first/promo");
                    }

                    android.put("androidChannelId", "androidChannelId");
                    android.put("tag", "tag");
                    android.put("collapseKey", "type_a");
                    android.put("smallIcon", "ic_baseline_android_24");
                    android.put("iconColor", "#0000FF");

                    if (isDeepLink) {
                        message += " & Button";
                        HashMap<String, Object> button1 = new HashMap<>();
                        button1.put("caption", "ACTION");
                        button1.put("action", "devino://first");
                        android.put("buttons", new HashMap[]{button1});
                    }

                    if (isPicture) {
                        message += " & Picture";
                        android.put(
                                "image",
                                "https://images.unsplash.com/photo-1707343848655-a196bfe88861?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDF8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                        );
                    }

                    if (isSound) {
                        String sound = ContentResolver.SCHEME_ANDROID_RESOURCE
                                + "://"
                                + context.getPackageName()
                                + "/" + R.raw.push_sound;
                        Log.d(context.getString(R.string.logs_tag), "sound = " + sound);
                        android.put("sound", sound);
                        // or use method setCustomSound(sound):
                        DevinoSdk.getInstance().setCustomSound(Uri.parse(sound));
                    } else {
                        DevinoSdk.getInstance().useDefaultSound();
                    }

                    body.put("android", android);
                    body.put("text", message);

                    HashMap<String, Object>[] arr = new HashMap[1];
                    arr[0] = body;
                    devinoPushApi.sendPush(arr)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    json -> {
                                        callback.onMessageLogged(
                                                context.getString(R.string.push_send)
                                                        + " "
                                                        + retrofitClientInstance.getCurrentRequestUrl()
                                                        + " -> "
                                                        + json.toString()
                                        );
                                        Log.d(context.getString(R.string.logs_tag), json.toString());
                                    },
                                    throwable -> {
                                        callback.onMessageLogged(
                                                context.getString(R.string.push_not_send)
                                                        + " "
                                                        + retrofitClientInstance.getCurrentRequestUrl()
                                                        + " -> "
                                                        + throwable.getLocalizedMessage()
                                        );
                                        Log.d(
                                                context.getString(R.string.logs_tag),
                                                throwable.getLocalizedMessage()
                                        );
                                    }
                            );
                });
        firebaseInstanceId.getToken().addOnFailureListener(error -> {
            callback.onMessageLogged(
                    context.getString(R.string.push_not_send_token_not_got)
                            + " "
                            + error.getLocalizedMessage()
            );
        });
    }
}