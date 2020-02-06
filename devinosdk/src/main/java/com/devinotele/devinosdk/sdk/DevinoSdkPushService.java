package com.devinotele.devinosdk.sdk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.devinotele.devinosdk.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class DevinoSdkPushService extends FirebaseMessagingService {

    Gson gson = new Gson();
    private String channelId = "devino_push";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {

            Map<String, String> data = remoteMessage.getData();

            String pushId = data.get("pushId");

            if (pushId == null) return;

            String icon = data.get("icon");
            String title = data.get("title");
            String body = data.get("body");

            String buttonsJson = data.get("buttons");
            Type listType = new TypeToken<List<PushButton>>() {
            }.getType();
            List<PushButton> buttons = gson.fromJson(buttonsJson, listType);

            Uri sound = DevinoSdk.getInstance().getSound();

            boolean isSilent = "true".equalsIgnoreCase(data.get("silentPush"));
            if (!isSilent) {
                showSimpleNotification(title, body, R.drawable.ic_grey_circle, icon, buttons, true, sound, pushId);
            }

            DevinoSdk.getInstance().pushEvent(pushId, DevinoSdk.PushStatus.DELIVERED, null);
        }

    }

    void showSimpleNotification(String title, String text, int smallIcon, String largeIcon, List<PushButton> buttons, Boolean bigPicture, Uri sound, String pushId) {

        Intent broadcastIntent = new Intent(getApplicationContext(), DevinoPushReceiver.class);
        broadcastIntent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, DevinoPushReceiver.KEY_DEFAULT_ACTION);
        broadcastIntent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);

        Intent deleteIntent = new Intent(getApplicationContext(), DevinoCancelReceiver.class);
        deleteIntent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);

        PendingIntent defaultPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), broadcastIntent.hashCode(), broadcastIntent, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), deleteIntent.hashCode(), deleteIntent, PendingIntent.FLAG_ONE_SHOT);

        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(defaultPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setSound(null)
                .setChannelId(channelId)
                .setSmallIcon(smallIcon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (buttons != null && buttons.size() > 0) {
            for (PushButton button : buttons) {
                if (button.text != null) {
                    Intent intent = new Intent(this, DevinoPushReceiver.class);
                    intent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, button.deeplink);
                    intent.putExtra(DevinoPushReceiver.KEY_PICTURE, button.pictureLink);
                    intent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), button.hashCode(), intent, PendingIntent.FLAG_ONE_SHOT);
                    builder.addAction(R.drawable.ic_grey_circle, button.text, pendingIntent);
                }
            }
        }

        if (largeIcon != null) {
            Bitmap bitmap = ImageDownloader.getBitmapFromURL(largeIcon);
            if (bigPicture)
                builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
            builder.setLargeIcon(bitmap);
        } else

            playRingtone(sound);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(113, builder.build());

    }

    private void playRingtone(Uri customSound) {
        Uri notificationSound = customSound != null ? customSound : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
        r.play();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, "devino", importance);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationChannel.setSound(null, null);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    class PushButton {

        @SerializedName("text")
        private String text;

        @SerializedName("deeplink")
        private String deeplink;

        @SerializedName("picture")
        private String pictureLink;

        PushButton(String text, String deeplink, String pictureLink) {
            this.text = text;
            this.deeplink = deeplink;
            this.pictureLink = pictureLink;
        }

        String getText() {
            return text;
        }

        void setText(String text) {
            this.text = text;
        }

        String getDeeplink() {
            return deeplink;
        }

        void setDeeplink(String deeplink) {
            this.deeplink = deeplink;
        }

        String getPictureLink() {
            return pictureLink;
        }

        void setPictureLink(String pictureLink) {
            this.pictureLink = pictureLink;
        }
    }
}
