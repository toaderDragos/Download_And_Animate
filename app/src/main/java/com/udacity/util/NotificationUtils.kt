package com.udacity.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.MainActivity
import com.udacity.R

// Notification ID.
private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

// extension function to send messages (GIVEN)
/**
 * Builds and delivers the notification. If you want to pass some info to a different Activity - for example to Detail Activity
 * then add some putExtras to contentIntent. This details are taken as parameters from the result of the DownloadManager download.
 *
 * @param context, activity context.
 */
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context, chosenTitle: String, successState: String) {
    // Create the content intent for the notification, which launches this activity
    val contentIntent = Intent(applicationContext, DetailActivity::class.java)
    contentIntent.putExtra("chosen title", chosenTitle)
    contentIntent.putExtra("success or failure", successState)
    Log.i("dra", "what is the chosen Title in NotificationsUtil:${chosenTitle}")

    //  create PendingIntent
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // add style
    val bitmap = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.ic_baseline_cloud_download_24
    )
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(bitmap)
        .bigLargeIcon(null)

    // get an instance of NotificationCompat.Builder
    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.download_notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_baseline_cloud_download_24)// set title, text and icon to builder
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)  // set content intent
        .setAutoCancel(true)
        .setStyle(bigPicStyle)  // add style to builder - does not work at this moment
        .setLargeIcon(bitmap)
        .addAction(R.drawable.ic_baseline_lightbulb_24, "Check the status", contentPendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notify(NOTIFICATION_ID, builder.build())
}