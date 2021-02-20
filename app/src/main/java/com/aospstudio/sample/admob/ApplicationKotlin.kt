package com.aospstudio.sample.admob

import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds

class ApplicationKotlin : MultiDexApplication() {

    var appOpenManager: AppOpenManagerKotlin? = null

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) { }
        appOpenManager = AppOpenManagerKotlin(this)
    }
}
