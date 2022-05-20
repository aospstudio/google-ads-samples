package com.aospstudio.sample.admob

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aospstudio.sample.admob.ads.AppOpenAdManager

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = application as? AppOpenAdManager
        if (application == null) {
            startMainActivity()
            return
        }
        application.showAdIfAvailable(
            this@LauncherActivity,
            object : AppOpenAdManager.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    startMainActivity()
                }
            })
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
