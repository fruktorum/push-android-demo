package com.devinotele.devinosdk.sdk;


import android.annotation.SuppressLint;
import android.os.Build;

import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.Single;


class RetrofitHelper {

    private DevinoApi devinoApi;
    private String applicationId;
    private String token;

    RetrofitHelper(String apiKey, String applicationId, String token) {
        devinoApi = RetrofitClientInstance.getRetrofitInstance(apiKey).create(DevinoApi.class);
        this.applicationId = applicationId;
        this.token = token;
    }

    void setToken(String token) {
        this.token = token;
    }

    Single<JsonObject> registerUser(String email, String phone, HashMap<String, Object> customData) {
        HashMap<String, Object> body = getGenericBody();
        body.put("email", email);
        body.put("phone", phone);
        if (customData != null) body.put("customData", customData);
        return devinoApi.registerUser(token, body);
    }


    Single<JsonObject> changeSubscription (Boolean subscribed) {
        HashMap<String, Object> body = getGenericBody();
        body.put("subscribed", subscribed);
        return devinoApi.subscription(token, body);
    }

    Single<JsonObject> appStarted(Boolean subscribed, String appVersion) {
        HashMap<String, Object> body = getGenericBody();
        body = addCustomData(body);
        body.put("appVersion", appVersion);
        body.put("subscribed", subscribed);
        return devinoApi.appStart(token, body);
    }

    Single<JsonObject> customEvent(String eventName, HashMap<String, Object> eventData) {
        HashMap<String, Object> body = getGenericBody();
        body.put("eventName", eventName);
        body.put("eventData", eventData);
        return devinoApi.event(token, body);
    }

    Single<JsonObject> geo(Double latitude, Double longitude) {
        HashMap<String, Object> body = getGenericBody();
        body.put("latitude", latitude);
        body.put("longitude", longitude);
        return devinoApi.geo(token, body);
    }

    Single<JsonObject> pushEvent(String pushId, String actionType, String actionId) {
        HashMap<String, Object> body = getGenericBody();
        body.put("pushToken", token);
        body.put("pushId", pushId);
        body.put("actionType", actionType);
        body.put("actionId", actionId);
        return devinoApi.pushEvent(body);
    }

    @SuppressLint("SimpleDateFormat")
    private String getTimestamp() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    HashMap<String, Object> getGenericBody() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("reportedDateTimeUtc", getTimestamp());
        body.put("applicationId", applicationId);
        return body;
    }

    HashMap<String, Object> addCustomData(HashMap<String, Object> body) {
        body.put("platform", "ANDROID");
        body.put("osVersion", String.valueOf(Build.VERSION.SDK_INT));
        body.put("language", Locale.getDefault().getISO3Language().substring(0, 2));
        return body;
    }


}
