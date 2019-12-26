package com.devinotele.devinosdk.sdk;

import com.google.gson.JsonObject;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


class DevinoNetworkRepositoryImpl implements DevinoNetworkRepository {

    private RetrofitHelper retrofitHelper;
    private volatile DevinoLogsCallback callback;
    HashMap<Integer, Integer> retryMap = new HashMap<>();

    DevinoNetworkRepositoryImpl(String apiKey, String applicationId, String token, DevinoLogsCallback callback) {
        retrofitHelper = new RetrofitHelper(apiKey, applicationId, token);
        this.callback = callback;
    }

    private <T> Observable<T> retryOnHttpError(Observable<T> source) {
        return retryOnHttpError(60L, source);
    }

    private <T> Observable<T> retryOnHttpError(Long interval, Observable<T> source) {
        retryMap.put(source.hashCode(), 0);
        return source.retryWhen(errors ->
                errors.flatMap(error -> {
                    callback.onMessageLogged("ERROR");
                    boolean retryCondition = error instanceof HttpException && codeToRepeat(((HttpException) error).code());
                    int retryCount = 3;
                    if(retryMap.get(source.hashCode()) != null) retryCount = retryMap.get(source.hashCode());
                    if (retryCount < 3 && retryCondition) {
                        retryMap.put(source.hashCode(), retryCount + 1);
                        return Observable.timer(interval, TimeUnit.SECONDS);
                    }
                    // For anything else, don't retry
                    return Observable.error(error);
                })
        );
    }

    private Boolean codeToRepeat(int errorCode) {
        return errorCode != 200 && !(errorCode >=400 && errorCode <= 404);
    }

    private <T> Observable<T> retryOnHttpError(Single<T> source) {
        return retryOnHttpError(source.toObservable());
    }

    @Override
    public Observable<JsonObject> registerUser(String email, String phone) {
        return registerUser(email, phone, null);
    }

    @Override
    public Observable<JsonObject> registerUser(String email, String phone, HashMap<String, Object> customData) {
        return retryOnHttpError(retrofitHelper.registerUser(email, phone, customData));
    }

    @Override
    public Observable<JsonObject> changeSubscription(Boolean subscribed) {
        return retryOnHttpError(retrofitHelper.changeSubscription(subscribed));
    }

    @Override
    public Observable<JsonObject> appStarted(String appVersion, Boolean subscribed) {
        return retryOnHttpError(retrofitHelper.appStarted(subscribed, appVersion));
    }

    @Override
    public Observable<JsonObject> customEvent(String eventName, HashMap<String, Object> eventData) {
        return retryOnHttpError(retrofitHelper.customEvent(eventName, eventData));
    }

    @Override
    public Single<JsonObject> geo( Double latitude, Double longitude) {
        return retrofitHelper.geo(latitude, longitude)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<JsonObject> pushEvent(String pushId, String actionType, String actionId) {
        return retryOnHttpError(retrofitHelper.pushEvent(pushId, actionType, actionId));
    }

    @Override
    public void updateToken(String token) {
        retrofitHelper.setToken(token);
    }
}
