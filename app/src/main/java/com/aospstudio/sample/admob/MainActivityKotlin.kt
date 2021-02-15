package com.aospstudio.sample.admob

import androidx.appcompat.app.AppCompatActivity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentForm
import android.os.Bundle
import com.aospstudio.sample.admob.R
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.android.ump.ConsentInformation.OnConsentInfoUpdateSuccessListener
import com.google.android.ump.ConsentInformation.OnConsentInfoUpdateFailureListener
import com.google.android.ump.FormError
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.ump.UserMessagingPlatform.OnConsentFormLoadSuccessListener
import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener
import com.google.android.ump.UserMessagingPlatform.OnConsentFormLoadFailureListener
import android.view.Display
import android.util.DisplayMetrics
import android.widget.FrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd

class MainActivityKotlin : AppCompatActivity() {

    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null
    private var mAdView: AdView? = null
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUAMPForm()
    }

    private fun initUAMPForm() {
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this,
            params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    initConsentForm()
                }
            },
            { })
        MobileAds.initialize(this) {
            loadBanner()
            loadInterstitial()
        }
        MobileAds.setAppMuted(true)
    }

    private fun initConsentForm() {
        UserMessagingPlatform.loadConsentForm(this, { consentForm: ConsentForm ->
            this.consentForm = consentForm
            if (consentInformation!!.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(this) { initConsentForm() }
            }
        }
        ) { }
    }

    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val widthPixels = outMetrics.widthPixels.toFloat()
            val density = outMetrics.density
            val adWidth = (widthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    private fun loadBanner() {
        mAdView = AdView(this)
        mAdView!!.adUnitId = "ca-app-pub-3940256099942544/9214589741"
        val adRequest = AdRequest.Builder().build()
        val adviewLayout = findViewById<FrameLayout>(R.id.adviewLayout)
        adviewLayout.addView(mAdView)
        val adSize = adSize
        mAdView!!.adSize = adSize
        mAdView!!.loadAd(adRequest)
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    mInterstitialAd!!.show(this@MainActivityKotlin)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                }
            })
    }
}
