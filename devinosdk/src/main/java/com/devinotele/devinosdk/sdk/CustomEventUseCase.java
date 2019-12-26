package com.devinotele.devinosdk.sdk;


import android.annotation.SuppressLint;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


class CustomEventUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;
    private String eventTemplate = "custom event (%s) ";

    CustomEventUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    @SuppressLint("CheckResult")
    void run(String eventName, HashMap<String, Object> eventData) {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);
        if (token.length() > 0) trackSubscription(networkRepository.customEvent(eventName, eventData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        json -> logsCallback.onMessageLogged(String.format(eventTemplate, eventName) + json.toString()),
                        throwable -> {
                            if (throwable instanceof HttpException)
                                logsCallback.onMessageLogged(getErrorMessage(String.format(eventTemplate, eventName), ((HttpException) throwable)));
                            else
                                logsCallback.onMessageLogged(String.format(eventTemplate, eventName) + throwable.getMessage());

                        }
                )
        );

        else logsCallback.onMessageLogged("can't send custom event -> token not registered");
    }

}
