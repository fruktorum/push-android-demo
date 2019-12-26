package com.devinotele.devinosdk.sdk;


import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


class SendGeoUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;
    private String eventTemplate = "geo(%s, %s) ";

    SendGeoUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run(Double latitude, Double longitude) {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);
        if(token.length() > 0) trackSubscription(networkRepository.geo(latitude, longitude)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        json -> logsCallback.onMessageLogged(String.format(eventTemplate, latitude, longitude) + json.toString()),
                        throwable -> {
                            if (throwable instanceof HttpException)
                                logsCallback.onMessageLogged(getErrorMessage(String.format(eventTemplate, latitude, longitude), ((HttpException) throwable)));
                            else
                                logsCallback.onMessageLogged(String.format(eventTemplate, latitude, longitude)+ throwable.getMessage());
                        }
                )
        );
        else logsCallback.onMessageLogged("can't send geo -> token not registered");
    }

}
