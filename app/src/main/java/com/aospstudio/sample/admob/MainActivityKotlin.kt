@file:Suppress("DEPRECATION")

package com.aospstudio.sample.admob

import androidx.appcompat.app.AppCompatActivity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentForm
import android.widget.FrameLayout
import android.os.Bundle
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import android.util.DisplayMetrics
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd

class MainActivityKotlin : AppCompatActivity() {

    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null
    private var adviewLayout: FrameLayout? = null
    private var mAdView: AdView? = null
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adviewLayout = findViewById(R.id.adviewLayout)
        adviewLayout!!.post { initUAMPForm() }
    }

    private fun initUAMPForm() {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation!!.requestConsentInfoUpdate(this,
            params,
            {
                if (consentInformation!!.isConsentFormAvailable) {
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
            val density = outMetrics.density
            var adWidthPixels = adviewLayout!!.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationBannerAdSizeWithWidth(this, adWidth)
        }

    private fun loadBanner() {
        mAdView = AdView(this)
        mAdView!!.adUnitId = "ca-app-pub-3940256099942544/9214589741"
        adviewLayout!!.removeAllViews()
        adviewLayout!!.addView(mAdView)
        val adSize = adSize
        mAdView!!.adSize = adSize
        val adRequest = AdRequest.Builder().build()
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

    public override fun onPause() {
        if (mAdView != null) {
            mAdView!!.pause()
        }
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        if (mAdView != null) {
            mAdView!!.resume()
        }
    }

    public override fun onDestroy() {
        if (mAdView != null) {
            mAdView!!.destroy()
        }
        super.onDestroy()
    }
}
