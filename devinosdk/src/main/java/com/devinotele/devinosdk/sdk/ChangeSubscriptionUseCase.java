package com.devinotele.devinosdk.sdk;


import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


class ChangeSubscriptionUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;
    private String eventTemplate = "set subscribed (%s)";

    ChangeSubscriptionUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run(Boolean subscribed) {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);

        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_SUBSCRIBED, subscribed);
        if (token.length() > 0) trackSubscription(networkRepository.changeSubscription(subscribed)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        json -> logsCallback.onMessageLogged(String.format(eventTemplate, subscribed.toString()) + " -> " + json.toString()),
                        throwable -> {
                            if (throwable instanceof HttpException)
                                logsCallback.onMessageLogged(getErrorMessage(String.format(eventTemplate, subscribed.toString()), ((HttpException) throwable)));
                            else
                                logsCallback.onMessageLogged(String.format(eventTemplate, subscribed.toString()) + " -> " + throwable.getMessage());

                        }
                )
        );
        else logsCallback.onMessageLogged("can't set subscribed -> token not registered");
    }
}
