package com.example.foodhub

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton

class AboutUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Video Implementation
        val videoView = findViewById<VideoView>(R.id.videoPromo)
        
        // Using the local video file from res/raw/promo_video
        val videoPath = "android.resource://" + packageName + "/" + R.raw.promo_video
        videoView.setVideoURI(Uri.parse(videoPath))
        
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        
        // Loop the video
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
        }

        videoView.start()

        // New PDF Download Button
        findViewById<MaterialButton>(R.id.btnDownloadPDF).setOnClickListener {
            PdfGenerator.generateAndSharePdf(this)
        }

        // Database Export Button
        findViewById<MaterialButton>(R.id.btnExportDB).setOnClickListener {
            DatabaseExporter.exportDatabase(this)
        }
    }
}
