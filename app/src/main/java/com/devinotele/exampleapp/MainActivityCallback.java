package com.devinotele.exampleapp;

import com.devinotele.devinosdk.sdk.DevinoLogsCallback;
import io.reactivex.subjects.ReplaySubject;

public interface MainActivityCallback {
    DevinoLogsCallback getLogsCallback();
    ReplaySubject<String> getLogs();
}
