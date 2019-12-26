## Devino Example Application

The purpose of this application is to show the proper implementation of Devino Sdk Library


## Devino SDK

Devino SDK has a functionality to handle push notifications


### Integration via AAR file
1. Download latest library *.aar file from repository.
2. Put aar file into your project libs folder
3. In your top-level build.gradle file add this
```
...
allprojects {
    repositories {
        ...
        flatDir {
            dirs 'libs'
        }
        
    }
}
...
```
4. In your module-level build.gradle add the following line
```
implementation(name:'devinosdk-release-<VERSION>', ext:'aar')
```


### Implementation

To make things work you need Firebase in your application.

If you don't have it already, start from here:
* [General Firebase Setup](https://firebase.google.com/docs/android/setup?authuser=0)
* [Firebase Cloud Messaging Setup](https://firebase.google.com/docs/cloud-messaging/android/client?authuser=0)


Once you have firebase set up, instantiate library with a builder.
In our example app we do it in Application class

```
public class DevinoExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseInstanceId firebase = FirebaseInstanceId.getInstance();
        
        String devinoSecretKey = "Secret Key";
        String appId = "Application ID";
        
        DevinoSdk.Builder builder = new DevinoSdk.Builder(this, devinoSecretKey, appId, firebase);
        builder.build();
    }
}
```


### Get sdk logs

To subscribe for sdk logs create instance of DevinoLogsCallback

this way

```
DevinoLogsCallback logs = new DevinoLogsCallback() {
    @Override
    public void onMessageLogged(String sdkLogMessage) {
        System.out.println(sdkLogMessage);
    }
};
```

or this way

```
DevinoLogsCallback logs = sdkLogMessage -> {
    System.out.println(sdkLogMessage);
};
```

then subscribe for updates


```
DevinoSdk.getInstance().requestLogs(logs);
```

Don't forget to unsubscribe it, when you don't need it anymore
```
DevinoSdk.getInstance().unsubscribeLogs();
```


### Register/update user data
Update user data this way

```
DevinoSdk.getInstance().register("89998887766", "example@email.com");
```

Phone and email must be valid. Otherwise server will not accept it.

### Send device geo information
You can ask sdk to collect user geo and send it to a server with a specified interval.
Due to Android OS restrictions it is not guaranteed that this function always works on every device.
Some devices may restrict scheduled background tasks. Single geo updates can be also rescheduled by OS (some updates may come later than expected).

Be aware. Updates will stop if a device was rebooted.
Geo updates need user permission to be granted. Sdk can help it as well.

Start geo updates calling 
```
int intervalMinutes = 15;
DevinoSdk.getInstance().subscribeGeo(this, intervalMinutes);
```

Get permission with
```
int REQUEST_CODE_START_UPDATES = <SOME CODE>
DevinoSdk.getInstance().requestGeoPermission(this, REQUEST_CODE_START_UPDATES);
```

To handle permission dialog result override onRequestPermissionsResult() in your activity

```
@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_START_UPDATES: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logsCallback.onMessageLogged("GEO PERMISSION GRANTED");
                    //do what you need
                } else {
                    //PERMISSION DENIED
                }
            }
            break;
            ...

        }
    }
```

Unsubscribe updates with
```
DevinoSdk.getInstance().unsubscribeGeo(context);
```


### Report app started

Do it with

```
DevinoSdk.getInstance().appStarted();
```


### Send custom event

You can send any data like this
```
DevinoSdk.getInstance()
    .sendEvent(
        "Event name", 
        new HashMap<String, Object>() {{
            put("Foo", "Bar");
        }});
```


### Report push status

When devino push is received, opened or canceled you can send a report on that.
It is highly recommended that you use sdk constants for message status

```
DevinoSdk.getInstance().pushEvent(pushId, DevinoSdk.PushStatus.DELIVERED, null);
```