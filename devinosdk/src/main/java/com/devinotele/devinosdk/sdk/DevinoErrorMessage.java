package com.devinotele.devinosdk.sdk;

class DevinoErrorMessage {
    private String event;
    private String code;
    private String message;

    DevinoErrorMessage(String event, String code, String message) {
        this.event = event;
        this.code = code;
        this.message = message;
    }

    String getMessage() {
        return event + ": ERROR\n" + "code: " + code + ",  message: " + message;
    }
}
