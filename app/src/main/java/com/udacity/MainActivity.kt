package com.udacity

/**     @DrStart:   */

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var customButton: LoadingButton
    private lateinit var downloadOptions: RadioGroup
    private lateinit var notificationManager: NotificationManager

    private val downloadUrls = mapOf(
        // TODO @DrStart:     Add actual URLs for these options.
        R.id.glideOption to "https://github.com/bumptech/glide",
        R.id.udacityOption to "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter",
        R.id.retrofitOption to "https://github.com/square/retrofit"
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

        /**     @DrStart:  Enable request permission for devices running Android 13 or higher */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        downloadOptions.setOnCheckedChangeListener { _, checkedId ->
            selectedUrl = downloadUrls[checkedId]
        }

        customButton.setOnClickListener {
            selectedUrl?.let { url ->
                download(url)
                customButton.setLoading(true)
                // Start the download timeout check
                startDownloadTimeout(8000, downloadID)
            } ?: Toast.makeText(
                this,
                getString(R.string.select_option_to_download),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(
                    this,
                    "Notification permission is required for downloads",
                    Toast.LENGTH_LONG
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

                if (downloadStatus != getString(R.string.success)) {
                    onDownloadFailed(id)
                } else {
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

    private fun startDownloadTimeout(timeout: Long, downloadId: Long) {
        val startTime = System.currentTimeMillis()

        val checkStatusRunnable = object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime >= timeout) {
                    val status = getDownloadStatus(downloadId)
                    if (status != getString(R.string.success)) {
                        onDownloadFailed(downloadId)
                    }
                } else {
                    // Re-run the Runnable after a delay until the timeout is reached
                    Handler(Looper.getMainLooper()).postDelayed(this, 1000)
                }
            }
        }

        // Start the repeated status check
        Handler(Looper.getMainLooper()).postDelayed(checkStatusRunnable, 1000)
    }

    private fun onDownloadFailed(downloadId: Long) {
        customButton.onDownloadFail() // Reset the button state
        val repoName = getRepositoryName()
        showNotification(
            title = getString(R.string.notification_title),
            text = getString(R.string.download_failed_text, repoName),
            downloadId = downloadId,
            repoName = repoName,
            status = getString(R.string.failed)
        )
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
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            downloadId.toInt(),
            detailIntent,
            pendingIntentFlags
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