package com.devinotele.devinosdk.sdk;

import com.google.gson.JsonObject;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Single;


interface DevinoNetworkRepository {

    Observable<JsonObject> registerUser(String email, String phone, HashMap<String, Object> customData);
    Observable<JsonObject> registerUser(String email, String phone);
    Observable<JsonObject> changeSubscription(Boolean subscribed);
    Observable<JsonObject> appStarted(String appVersion, Boolean subscribed);
    Observable<JsonObject> customEvent(String eventName, HashMap<String, Object> eventData);
    Single<JsonObject> geo(Double latitude, Double longitude);
    Observable<JsonObject> pushEvent(String pushId, String actionType, String actionId);
    void updateToken(String token);

}


