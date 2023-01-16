package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.util.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var chosenUrl: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // if we have to add many more buttons I would have used dataBinding instead
        val radioButtonLoad: RadioButton = findViewById(R.id.radioButtonLoad)
        val radioButtonGlide: RadioButton = findViewById(R.id.radioButtonGlide)
        val radioButtonRetrofit: RadioButton = findViewById(R.id.radioButtonRetrofit)
        val downloadTextOnProgressBar: TextView = findViewById(R.id.download_text)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        val toastText = Toast.makeText(this, "Please select a file to download!", Toast.LENGTH_SHORT)
        toastText.show()

        // here was the custom buttoin clicklistener - remake it into a nice function
        // don't confuse isActivated with isChecked
        custom_button.setOnClickListener {
            if (radioButtonLoad.isChecked) {
                helperFunction(radioButtonLoad, URL_LOAD, downloadTextOnProgressBar)

            } else if (radioButtonGlide.isChecked){
                helperFunction(radioButtonGlide, URL_GLIDE, downloadTextOnProgressBar)

            } else if (radioButtonRetrofit.isChecked){
                helperFunction(radioButtonRetrofit, URL_RETRO, downloadTextOnProgressBar)

            } else {
                Toast.makeText(this, "Choose an option", Toast.LENGTH_SHORT).show()
            }
        //      We don't implement this here - the completed function -  because it didn't finish downloading yet
        //      custom_button.changeButtonStateinMAinActivity(ButtonState.Completed)
        }

        // A channel for the notification
        createChannel(
            getString(R.string.download_notification_channel_id),
            getString(R.string.download_notification_channel_name)
        )
    }

    private fun helperFunction(radioButtonLoad: RadioButton, url: String, txtOnProgressBar: TextView) {
        radioButtonLoad.isChecked = false
        custom_button.changeButtonStateinMAinActivity(ButtonState.Loading)
        txtOnProgressBar.setText(getString(R.string.downloading))
        when (url) {
            URL_LOAD -> chosenUrl = "Load App Repository"
            URL_GLIDE -> chosenUrl = "Glide Image Loading Library"
            URL_RETRO -> chosenUrl = "Type-safe Retrofit HTTP Client"
        }
        download(url)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Log.i("dra", "what is in id:${id}")
            if (id == downloadID) {


                // changing the displayed text
                val downloadTextOnProgressBar: TextView = findViewById(R.id.download_text)
                downloadTextOnProgressBar.text = getString(R.string.download)

                // fire the notification
                val notificationManager = context?.let {
                    ContextCompat.getSystemService(it, NotificationManager::class.java)
                } as NotificationManager

                // solving the title and the successful state of the download - from stackOverflow
                val query: DownloadManager.Query?
                val c: Cursor?
                val downloadManager: DownloadManager?
                val chosenTitle: String
                var successState = "Error"

                downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                query = DownloadManager.Query()
                if (query != null) {
                    query.setFilterByStatus(
                        DownloadManager.STATUS_FAILED or DownloadManager.STATUS_PAUSED or DownloadManager.STATUS_SUCCESSFUL or
                                DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PENDING
                    )
                } else {
                    return
                }
                c = downloadManager.query(query)
                if (c.moveToFirst()) {
                    val status: Int = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_PAUSED -> {}
                        DownloadManager.STATUS_PENDING -> {}
                        DownloadManager.STATUS_RUNNING -> {}
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            custom_button.changeButtonStateinMAinActivity(ButtonState.Completed)
                            successState = "Successful"
                        }
                        DownloadManager.STATUS_FAILED -> {
                            custom_button.changeButtonStateinMAinActivity(ButtonState.Completed)
                            successState = "Failed"
                        }
                    }

                    chosenTitle = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE))
                    // I save in the chosenUrl the selected file and I pass its info to the notification text message
                    notificationManager.sendNotification("The file $chosenUrl has been downloaded!", context, chosenTitle, successState)
                }
            }
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.download_notification_channel_description)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    private fun download(URL: String) {
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(chosenUrl)
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL_LOAD =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE = "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val URL_RETRO = "https://github.com/square/retrofit/archive/refs/heads/master.zip"
//        private const val CHANNEL_ID = "channelId"
    }


}
