package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var customButton: LoadingButton
    private lateinit var downloadOptions: RadioGroup

    //    TODO @DrStart:    get exact urls for each radioButton
    private val downloadUrls = mapOf(
        R.id.glideOption to "https://github.com/bumptech/glide",
        R.id.udacityOption to "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter",
        R.id.retrofitOption to "https://github.com/square/retrofit"
    )
    private var selectedUrl: String? = null

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        customButton =
            findViewById(R.id.custom_button)
        downloadOptions =
            findViewById(R.id.downloadOptions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID)
        }

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        downloadOptions.setOnCheckedChangeListener { _, checkedId ->
            selectedUrl = downloadUrls[checkedId]
        }

//        TODO @DrStart:   handle state of customButton upon onClick
        customButton.setOnClickListener {
            selectedUrl?.let { url ->
                download(url)
                customButton.setLoading(true)
            } ?: Toast.makeText(this, "Please select an option to download", Toast.LENGTH_SHORT)
                .show()
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val receiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            if (id == downloadID) {
                customButton.setLoading(false)
                showNotification("Download Complete", "The file has been downloaded", downloadID)
            }
        }
    }

    //    TODO @DrStart:   method to download specific file based on userInput from radioButtons
    private fun download(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(getString(R.string.app_name))
            .setDescription(getString(R.string.app_description))
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)
    }

    // TODO @DrStart:    Implement the logic to show a notification to the user
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(title: String, text: String, downloadId: Long) {

        // Intent to open DetailActivity
        val detailIntent = Intent(this, DetailActivity::class.java).apply {
            putExtra(EXTRA_DOWNLOAD_ID, downloadId) // Pass the download ID or any other data
        }
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss notification after click

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    // TODO @DrStart:    Implement the logic to create a notification channel
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String) {
        // Create a NotificationChannel object with the channel ID, the channel name, and the channel importance
        val channel =
            NotificationChannel(channelId, "Download Channel", NotificationManager.IMPORTANCE_HIGH)
        // Set the channel description
        channel.setDescription("This channel is for download notifications")
        // Get an instance of the NotificationManager class
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Register the notification channel
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val EXTRA_DOWNLOAD_ID = "EXTRA_DOWNLOAD_ID"
        private const val NOTIFICATION_ID = 1
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}
