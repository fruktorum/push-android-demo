package com.devinotele.exampleapp.network;


import android.annotation.SuppressLint;
import android.util.Log;

import com.devinotele.devinosdk.sdk.DevinoLogsCallback;
import com.devinotele.exampleapp.BuildConfig;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class RetrofitHelper {

    private FirebaseApi firebaseApi;
    private DevinoPushApi devinoPushApi;
    private DevinoLogsCallback callback;

    public RetrofitHelper(DevinoLogsCallback callback) {
        firebaseApi = RetrofitClientInstance.getRetrofitInstance().create(FirebaseApi.class);
        devinoPushApi = RetrofitClientInstance.getRetrofitInstanceForDevinoPush().create(DevinoPushApi.class);
        this.callback = callback;
    }

    @SuppressLint("CheckResult")
    public void sendPush(FirebaseMessaging firebaseInstanceId, Boolean picture, Boolean sound, Boolean deepLink) {
        firebaseInstanceId.getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    String token = task.getResult();
                    String message = "Simple push";
                    Log.d("TOKEN", token);

                    HashMap<String, Object> body = new HashMap<>();
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("title", "Devino");


                    if (deepLink) {
                        message += " & Button";
                        HashMap<String, Object> button = new HashMap<>();
                        button.put("text", "Action");
                        button.put("deeplink", "devino://first");
                        button.put("picture", "https://avatars.mds.yandex.net/get-pdb/163339/224697a1-db7d-4d02-a12f-aa70383fadc3/s1200");
                        data.put("buttons", Arrays.asList(button));
                    }
                    if (picture) {
                        message += " & Picture";
                        data.put("icon", "https://avatars.mds.yandex.net/get-pdb/163339/224697a1-db7d-4d02-a12f-aa70383fadc3/s1200");
                    }

                    data.put("body", message);
                    body.put("to", token);
                    body.put("data", data);
                    firebaseApi.sendPush(body)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    object -> System.out.println(object.toString()),
                                    Throwable::printStackTrace);
                });
    }

    @SuppressLint("CheckResult")
    public void sendPushWithDevino(FirebaseMessaging firebaseInstanceId, Boolean picture, Boolean sound, Boolean deepLink) {
        firebaseInstanceId.getToken()
                .addOnCompleteListener(task -> {
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

                    HashMap<String, Object> options = new HashMap<>();
                    options.put("icon", "https://avatars.mds.yandex.net/get-pdb/163339/224697a1-db7d-4d02-a12f-aa70383fadc3/s1200");
                    body.put("options", options);

                    HashMap<String, Object> android = new HashMap<>();

                    android.put("action", "action");
                    android.put("iconColor", "iconColor");
                    android.put("sound", "sound");
                    android.put("androidChannelId", "androidChannelId");
                    android.put("tag", "tag");
                    android.put("collapseKey", "type_a");

                    if (deepLink) {
                        message += " & Button";
                        HashMap<String, Object> button1 = new HashMap<>();
                        button1.put("caption", "ACTION");
                        button1.put("action", "devino://first");
                        android.put("buttons", new HashMap[]{button1});
                    }

                    if (picture) {
                        message += " & Picture";
                        android.put("icon", "https://avatars.mds.yandex.net/get-pdb/163339/224697a1-db7d-4d02-a12f-aa70383fadc3/s1200");
                    }

                    body.put("android", android);
                    body.put("text", message);

                    HashMap<String, Object>[] arr = new HashMap[1];
                    arr[0] = body;
                    devinoPushApi.sendPush(arr)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    object -> {
                                        callback.onMessageLogged(object.toString());
                                        System.out.println(object);
                                    },
                                    error -> error.printStackTrace()
                            );
                });
    }
}
