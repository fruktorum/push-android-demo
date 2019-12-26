package com.devinotele.devinosdk.sdk;


import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


class AppStartedUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;
    private String event = "app started";

    AppStartedUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    public void run(String appVersion) {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);
        if (token.length() > 1) {
            Boolean subscribed = sharedPrefsHelper.getBoolean(SharedPrefsHelper.KEY_SUBSCRIBED);
            trackSubscription(networkRepository.appStarted(appVersion, subscribed)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            json -> logsCallback.onMessageLogged(event + json.toString()),
                            throwable -> {
                                if (throwable instanceof HttpException)
                                    logsCallback.onMessageLogged(getErrorMessage(event, ((HttpException) throwable)));
                                else
                                    logsCallback.onMessageLogged(event + throwable.getMessage());
                            }
                    )
            );
        } else logsCallback.onMessageLogged("application has no push token yet");
    }}
