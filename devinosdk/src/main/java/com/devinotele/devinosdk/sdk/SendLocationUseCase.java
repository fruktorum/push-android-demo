package com.devinotele.devinosdk.sdk;


import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;


class SendLocationUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;
    private String event = "send geo: ";

    SendLocationUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run() {
        trackSubscription(devinoLocationHelper.getNewLocation()
                .flatMap(location -> networkRepository.geo(location.getLatitude(), location.getLongitude()))
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
    }

}
