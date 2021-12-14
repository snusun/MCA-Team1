package com.example.aganada

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.aganada.camera.CameraXActivity
import com.example.aganada.views.InkManager
import com.example.aganada.views.ModelManager
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, CameraXActivity::class.java)
            startActivity(intent)
            finish()
        }, 750)

        CoroutineScope(Dispatchers.IO).launch {
            val modelManager = ModelManager()
            modelManager.setModel("ko")
            val isDownloaded = modelManager.checkIsModelDownloaded().await()
            if (!isDownloaded) {
                modelManager.download()
            }

        }
    }
}