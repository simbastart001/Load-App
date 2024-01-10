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
    private lateinit var notificationManager: NotificationManager

    private val downloadUrls = mapOf(
        R.id.glideOption to "https://github.com/bumptech/glide",
        R.id.udacityOption to "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter",
        R.id.retrofitOption to "https://`github.com/square/retrofit"
    )

    private val repositoryNames = mapOf(
        R.id.glideOption to R.string.glide_by_bumptech,
        R.id.udacityOption to R.string.udacity_project_starter_by_udacity,
        R.id.retrofitOption to R.string.retrofit_by_square
    )

    private var selectedUrl: String? = null
    private var downloadID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        customButton = findViewById(R.id.custom_button)
        downloadOptions = findViewById(R.id.downloadOptions)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID)
        }

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        downloadOptions.setOnCheckedChangeListener { _, checkedId ->
            selectedUrl = downloadUrls[checkedId]
        }

        customButton.setOnClickListener {
            selectedUrl?.let { url ->
                download(url)
                customButton.setLoading(true)
            } ?: Toast.makeText(
                this,
                getString(R.string.select_option_to_download),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            if (id == downloadID) {
                customButton.setLoading(false)

                val downloadStatus = getDownloadStatus(id)
                val selectedRepositoryName = getRepositoryName()

                showNotification(
                    title = getString(R.string.notification_title),
                    text = getString(R.string.notification_description),
                    downloadId = id,
                    repoName = selectedRepositoryName,
                    status = downloadStatus
                )
            }
        }
    }

    private fun getDownloadStatus(id: Long): String {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(id)
        val cursor = downloadManager.query(query)
        val downloadStatus = if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            when (cursor.getInt(statusIndex)) {
                DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.success)
                DownloadManager.STATUS_FAILED -> getString(R.string.failed)
                else -> getString(R.string.unknown)
            }
        } else {
            getString(R.string.error)
        }
        cursor.close()
        return downloadStatus
    }

    private fun getRepositoryName(): String {
        return repositoryNames[downloadOptions.checkedRadioButtonId]?.let { resId ->
            getString(resId)
        } ?: getString(R.string.unknown)
    }

    // TODO @DrStart:      Method to download specific file based on userInput from radioButtons
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

    // TODO @DrStart:      Method to show a notification to the user
    private fun showNotification(
        title: String,
        text: String,
        downloadId: Long,
        repoName: String,
        status: String
    ) {
        val detailIntent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_REPOSITORY_NAME, repoName)
            putExtra(DetailActivity.EXTRA_DOWNLOAD_STATUS, status)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            downloadId.toInt(),
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openDetailAction = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            getString(R.string.open),
            pendingIntent
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(openDetailAction)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    // TODO @DrStart:      Method to create a notification channel
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String) {
        val channel = NotificationChannel(
            channelId,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = getString(R.string.notification_channel_description)

        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
        private const val NOTIFICATION_ID = 1
    }
}