package com.autenticar.tuid.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
//import android.support.v4.app.NotificationCompat;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.autenticar.tuid.MainActivity;
import com.autenticar.tuid.R;
import com.autenticar.tuid.v2.HomeActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;


public class MyFirebaseMessagingService extends  FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String  from = remoteMessage.getFrom();
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.e("PUSH_NOTIFICATION", "From: " + remoteMessage.getFrom());
        
        if (remoteMessage.getData().size() > 0) {
            Log.e("PUSH_NOTIFICATION", "Message data payload: " + remoteMessage.getData());

            try{
                JSONObject data = new JSONObject(remoteMessage.getData());
                if(data.has("action") && data.getString("action").equalsIgnoreCase("life")){
                    Log.e("NOT", "ACTION!");
                    Intent resultIntent = new Intent(this, HomeActivity.class);
                    resultIntent.putExtra("action", "life");
                    JSONObject info = new JSONObject(data.getString("info"));
                    resultIntent.putExtra("dni", info.getString("document"));
                    resultIntent.putExtra("sexo", info.getString("gender"));
                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);

                    NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    
                    String id = "channel_1";
                    String description = "143";
                    int importance = NotificationManager.IMPORTANCE_LOW;
                    NotificationChannel channel = new NotificationChannel(id, description, importance);
                    channel.enableLights(true);
                    channel.enableVibration(true);//
                    manager.createNotificationChannel(channel);

                    String title =   "Titulo";
                    String text = "texto";
                    try{
                        title = remoteMessage.getNotification().getTitle();
                        text = remoteMessage.getNotification().getBody();
                    }
                    catch (Exception e){

                    }

                    Notification notification = new Notification.Builder(this, id)
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setSmallIcon(R.drawable.logovidanoti)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true)
                            .build();
                    manager.notify(1, notification);
                    return;
                }
            }
            catch (Exception e){
                Log.e("EX", e.getMessage());
            }

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            //System.out.println("MyFirebaseMessagingService Body: "+remoteMessage.getNotification().getBody());
            Log.e("PUSH_NOTIFICATION", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void scheduleJob() {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
    }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody, String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.logovida)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
