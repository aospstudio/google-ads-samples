package com.aospstudio.sample.admob;

import androidx.multidex.MultiDexApplication;

import com.google.android.gms.ads.MobileAds;

public class ApplicationJava extends MultiDexApplication {

    AppOpenManagerJava appOpenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, initializationStatus -> {
        });

        appOpenManager = new AppOpenManagerJava(this);
    }
}
