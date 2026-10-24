package com.bilgilimedia.mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

final class NotificationHelper {
    static final String CHANNEL_ID="bilgilimedia_updates";
    private NotificationHelper(){}
    static void ensureChannel(Context context){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O)return;
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE); if(nm==null)return;
        NotificationChannel ch=new NotificationChannel(CHANNEL_ID,context.getString(R.string.notification_channel_name),NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription(context.getString(R.string.notification_channel_description)); nm.createNotificationChannel(ch);
    }
    static void show(Context context,String articleId,String category,String title,String body){
        ensureChannel(context);
        Intent intent=new Intent(context,MainActivity.class); intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(articleId!=null&&!articleId.isEmpty()){intent.putExtra("open_article_id",articleId);intent.setData(Uri.parse(DataRepository.HOST_BASE+"/content/"+Uri.encode(articleId)));}
        int requestCode=articleId==null?1409:articleId.hashCode();
        PendingIntent pending=PendingIntent.getActivity(context,requestCode,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=Build.VERSION_CODES.O?new Notification.Builder(context,CHANNEL_ID):new Notification.Builder(context);
        b.setSmallIcon(R.drawable.ic_notification).setContentTitle(title==null||title.isEmpty()?"BilgiliMedia":title).setContentText(body==null?"":body)
                .setStyle(new Notification.BigTextStyle().bigText(body==null?"":body)).setAutoCancel(true).setContentIntent(pending);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE); if(nm!=null)nm.notify(requestCode,b.build());
    }
}
