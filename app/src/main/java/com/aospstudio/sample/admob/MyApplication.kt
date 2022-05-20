package com.aospstudio.sample.admob

import androidx.multidex.MultiDex
import com.aospstudio.sample.admob.ads.AppOpenAdManager
import com.google.android.gms.ads.MobileAds

class MyApplication : AppOpenAdManager() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(applicationContext)
        MobileAds.initialize(this)
    }
}
