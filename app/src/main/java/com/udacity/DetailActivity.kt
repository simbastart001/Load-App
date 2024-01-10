package com.udacity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var repositoryNameTextView: TextView
    private lateinit var downloadStatusTextView: TextView
    private lateinit var buttonDone: Button
    private lateinit var motionLayout: MotionLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        repositoryNameTextView = findViewById(R.id.repositoryNameTextView)
        downloadStatusTextView = findViewById(R.id.downloadStatusTextView)
        buttonDone = findViewById(R.id.buttonDone)
        motionLayout = findViewById(R.id.motionLayout)

        // TODO @DrStart:    Retrieve repository name and download status from Intent
        val repositoryName =
            intent.getStringExtra(EXTRA_REPOSITORY_NAME) ?: getString(R.string.unknown)
        val downloadStatus =
            intent.getStringExtra(EXTRA_DOWNLOAD_STATUS) ?: getString(R.string.unknown)

        repositoryNameTextView.text = getString(R.string.file_name, repositoryName)
        downloadStatusTextView.text = getString(R.string.status, downloadStatus)

        buttonDone.setOnClickListener {
            finish()
        }

    }

    companion object {
        const val EXTRA_REPOSITORY_NAME = "EXTRA_REPOSITORY_NAME"
        const val EXTRA_DOWNLOAD_STATUS = "EXTRA_DOWNLOAD_STATUS"
    }
}
