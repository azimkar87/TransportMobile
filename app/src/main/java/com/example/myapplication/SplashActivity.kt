package com.example.myapplication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.VideoView

class SplashActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        videoView=findViewById(R.id.videoView)
        val path="android.resource://"+packageName+"/"
        //+R.raw.splash
        val uri=Uri.parse(path)
        videoView!!.setVideoURI(uri)
        videoView!!.start()

        videoView!!.setOnCompletionListener {
            if (isFinishing)
            {
                true

            }
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}