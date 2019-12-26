package com.devinotele.devinosdk.sdk;


import com.google.gson.JsonObject;

import java.util.HashMap;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

interface DevinoApi {

    @PUT("users/{pushToken}/data")
    Single<JsonObject> registerUser(@Path("pushToken") String pushToken, @Body HashMap<String, Object> body);

    @POST("users/{pushToken}/app-start")
    Single<JsonObject> appStart(@Path("pushToken") String pushToken, @Body HashMap<String, Object> body);

    @POST("users/{pushToken}/event")
    Single<JsonObject> event(@Path("pushToken") String pushToken, @Body HashMap<String, Object> body);

    @POST("users/{pushToken}/geo")
    Single<JsonObject> geo(@Path("pushToken") String pushToken, @Body HashMap<String, Object> body);

    @POST("users/{pushToken}/subscription")
    Single<JsonObject> subscription(@Path("pushToken") String pushToken, @Body HashMap<String, Object> body);

    @POST("messages/events")
    Single<JsonObject> pushEvent(@Body HashMap<String, Object> body);

}