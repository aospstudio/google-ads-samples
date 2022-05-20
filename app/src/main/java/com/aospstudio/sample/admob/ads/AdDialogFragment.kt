package com.aospstudio.sample.admob.ads

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.aospstudio.sample.admob.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val AD_COUNTER_TIME = 5L

class AdDialogFragment : DialogFragment() {

    private var listener: AdDialogInteractionListener? = null
    private var countDownTimer: CountDownTimer? = null
    private var timeRemaining: Long = 0

    fun setAdDialogInteractionListener(listener: AdDialogInteractionListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view: View = requireActivity().layoutInflater.inflate(R.layout.widget_dialog_ad, null)

        val builder = MaterialAlertDialogBuilder(this.requireActivity())
        builder.setView(view)

        val args = arguments
        var rewardAmount = -1
        var rewardType: String? = null
        if (args != null) {
            rewardAmount = args.getInt(REWARD_AMOUNT)
            rewardType = args.getString(REWARD_TYPE)
        }
        if (rewardAmount > 0 && rewardType != null) {
            builder.setTitle(getString(R.string.reward_title))
        }

        builder.setNegativeButton(
            getString(R.string.negative_button_text)
        ) { _, _ -> dialog?.cancel() }
        val dialog: Dialog = builder.create()
        createTimer(AD_COUNTER_TIME, view)
        return dialog
    }

    private fun createTimer(time: Long, dialogView: View) {
        val textView: TextView = dialogView.findViewById(R.id.timer)
        countDownTimer = object : CountDownTimer(time * 1000, 50) {
            override fun onTick(millisUnitFinished: Long) {
                timeRemaining = millisUnitFinished / 1000 + 1
                textView.text =
                    String.format(getString(R.string.video_starting_in_text), timeRemaining)
            }

            override fun onFinish() {
                dialog?.dismiss()

                if (listener != null) {
                    listener!!.onShowAd()
                }
            }
        }
        countDownTimer?.start()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (listener != null) {
            listener!!.onCancelAd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        countDownTimer = null
    }

    interface AdDialogInteractionListener {
        fun onShowAd()

        fun onCancelAd()
    }

    companion object {
        private const val REWARD_AMOUNT = "rewardAmount"
        private const val REWARD_TYPE = "rewardType"

        fun newInstance(rewardAmount: Int, rewardType: String): AdDialogFragment {
            val args = Bundle()
            args.putInt(REWARD_AMOUNT, rewardAmount)
            args.putString(REWARD_TYPE, rewardType)
            val fragment = AdDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
