package com.devinotele.devinosdk.sdk;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

class BaseUC {

    SharedPrefsHelper sharedPrefsHelper;
    DevinoNetworkRepository networkRepository;
    NotificationsHelper notificationsHelper;
    DevinoLocationHelper devinoLocationHelper;
    static CompositeDisposable compositeDisposable = new CompositeDisposable();

    BaseUC(HelpersPackage hp) {
        this.sharedPrefsHelper = hp.getSharedPrefsHelper();
        this.networkRepository = hp.getNetworkRepository();
        this.notificationsHelper = hp.getNotificationsHelper();
        this.devinoLocationHelper = hp.getDevinoLocationHelper();
    }

    String getErrorMessage(String event, HttpException error) {
        String message;
        try {
            message = error.response().errorBody().string();
        } catch (Throwable e) {
            e.printStackTrace();
            message = error.getMessage();
        }

        DevinoErrorMessage errMsg = new DevinoErrorMessage(
                event,
                String.valueOf(error.code()),
                message
        );
        return errMsg.getMessage();
    }

    static void unsubscribeAll() {
        compositeDisposable.clear();
    }

    static void trackSubscription(Disposable disposable) {
        compositeDisposable.add(disposable);
    }
}
