package com.aospstudio.sample.admob

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aospstudio.sample.admob.ads.AdDialogFragment
import com.aospstudio.sample.admob.ads.AdUnitId
import com.aospstudio.sample.admob.databinding.ActivityMainBinding
import com.aospstudio.sample.admob.network.NetworkMonitorUtil
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

private const val COUNTER_TIME = 0L
private const val OVER_REWARD = 1

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val context: Context = this
    private val networkMonitor = NetworkMonitorUtil(context)
    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null
    private var initialLayoutComplete = false
    private var mAdIsLoading: Boolean = false
    private lateinit var adView: AdView
    private var interstitialAd: InterstitialAd? = null
    private var coinCount: Int = 0
    private var countDownTimer: CountDownTimer? = null
    private var isLoadingAds = false
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var timeRemaining: Long = 0L

    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adviewLayout.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context,
                adWidth
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkMonitor.result = { isAvailable, _ ->
            runOnUiThread {
                when (isAvailable) {
                    true -> {
                        MobileAds.initialize(context) { }
                        initConsentForm()
                        initBanner()
                        initStartApp()
                        if (rewardedInterstitialAd == null && !isLoadingAds) {
                            initLoadRewardedInterstitialAd()
                        }
                        binding.adviewLayout.visibility = View.VISIBLE
                    }
                    false -> {
                        binding.adviewLayout.visibility = View.GONE
                    }
                }
            }
        }

        val extras = Bundle()
        extras.putString("npa", "1")

        AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        binding.interstitialOpen.setOnClickListener { initLoadInterstitial() }

        binding.interstitialRewardsOpen.setOnClickListener { createTimer(COUNTER_TIME) }
    }

    private fun initConsentForm() {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation!!.requestConsentInfoUpdate(this, params, {
            if (consentInformation!!.isConsentFormAvailable) {
                initLoadForm()
            }
        }, {})
    }

    private fun initLoadForm() {
        UserMessagingPlatform.loadConsentForm(this, { consentForm ->
            this@MainActivity.consentForm = consentForm
            if (consentInformation!!.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(
                    this@MainActivity
                ) {
                    initLoadForm()
                }
            }
        }) {}
    }

    private fun initBanner() {
        adView = AdView(context)
        binding.adviewLayout.addView(adView)
        binding.adviewLayout.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                adView.adUnitId = AdUnitId.BANNER_AD_UNIT_ID
                adView.adSize = adSize
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
            }
        }
    }

    private fun initInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context, AdUnitId.INTERSTITIAL_AD_UNIT_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    mAdIsLoading = false
                }

                override fun onAdLoaded(mInterstitialAd: InterstitialAd) {
                    interstitialAd = mInterstitialAd
                    mAdIsLoading = false
                }
            }
        )
    }

    private fun initLoadInterstitial() {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    initInterstitial()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                }
            }
            interstitialAd?.show(this)
        } else {
            initStartApp()
        }
    }

    private fun initStartApp() {
        if (!mAdIsLoading && interstitialAd == null) {
            mAdIsLoading = true
            initInterstitial()
        }
    }

    private fun initLoadRewardedInterstitialAd() {
        if (rewardedInterstitialAd == null) {
            isLoadingAds = true
            val adRequest = AdRequest.Builder().build()

            RewardedInterstitialAd.load(
                context,
                AdUnitId.REWARD_AD_UNIT_ID,
                adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        isLoadingAds = false
                        rewardedInterstitialAd = null
                        createTimer(COUNTER_TIME)
                    }

                    override fun onAdLoaded(rewardedAd: RewardedInterstitialAd) {
                        super.onAdLoaded(rewardedAd)
                        rewardedInterstitialAd = rewardedAd
                        isLoadingAds = false
                    }
                })
        }
    }

    private fun addCoins(coins: Int) {
        coinCount += coins
    }

    private fun createTimer(time: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(time * 1000, 50) {
            override fun onTick(millisUnitFinished: Long) {
                timeRemaining = millisUnitFinished / 1000 + 1
            }

            override fun onFinish() {
                addCoins(OVER_REWARD)

                if (rewardedInterstitialAd == null) {
                    return
                }

                val rewardAmount = rewardedInterstitialAd!!.rewardItem.amount
                val rewardType = rewardedInterstitialAd!!.rewardItem.type
                introduceVideoAd(rewardAmount, rewardType)
            }
        }

        countDownTimer?.start()
    }

    private fun introduceVideoAd(rewardAmount: Int, rewardType: String) {
        val dialog = AdDialogFragment.newInstance(rewardAmount, rewardType)
        dialog.setAdDialogInteractionListener(object :
            AdDialogFragment.AdDialogInteractionListener {
            override fun onShowAd() {
                showRewardedVideo()
            }

            override fun onCancelAd() {
            }
        })
        dialog.show(supportFragmentManager, "AdDialogFragment")
    }

    private fun showRewardedVideo() {
        if (rewardedInterstitialAd == null) {
            return
        }

        rewardedInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedInterstitialAd = null
                initLoadRewardedInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedInterstitialAd = null
            }

            override fun onAdShowedFullScreenContent() {
            }
        }

        rewardedInterstitialAd?.show(
            this
        ) { rewardItem ->
            addCoins(rewardItem.amount)
            binding.count.text = "Earned: 1"
        }
    }

    @Override
    override fun onResume() {
        networkMonitor.register()
        super.onResume()
    }

    @Override
    override fun onPause() {
        networkMonitor.unregister()
        super.onPause()
    }
}
