package com.richardswesterhof.wakelightcompanion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.richardswesterhof.wakelightcompanion.broadcast_receivers.WakeLightStopper
import com.richardswesterhof.wakelightcompanion.devices.tuya.TuyaApiHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date


class MainFragment : Fragment() {

    private var dateMillis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            dateMillis = it.getLong("nextAlarmMillis", 0)
        }

        replacePlaceholders(view, dateMillis)

        val button = view.findViewById(R.id.cancel_next_alarm_button) as Button
        button.setOnClickListener { cancelNextWakeLight(view) }

        val magicButton = view.findViewById(R.id.test_button) as Button
        magicButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val tuyaApiHandler = TuyaApiHandler()
                Log.d(
                    "testing",
                    tuyaApiHandler.execute(
                        tuyaApiHandler.getTokenPath,
                        "GET",
                        "",
                        HashMap()
                    ).body?.string() ?: "<no response body>"
                )
            }
        }
    }


    fun cancelNextWakeLight(view: View) {
        // create an intent to stop the wakelight and broadcast it
        // so it can be received by tbe WakeLightStopper
        val stopWakeLightIntent = Intent(context, WakeLightStopper::class.java).apply {
            action = "com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM"
        }
        context?.sendBroadcast(stopWakeLightIntent)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        with(sharedPrefs.edit()) {
            putLong("nextAlarmMillis", 0)
            apply()
        }

        replacePlaceholders(view, 0)
    }


    private fun replacePlaceholders(view: View, dateMillis: Long) {
        val textView = view.findViewById<View>(R.id.text_next_user_alarm) as TextView?
        val nextDate = Date(dateMillis)
        textView?.text = nextDate.toString()
    }

    companion object {
        @JvmStatic
        fun newInstance(nextAlarmMillis: Long): MainFragment =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putLong("nextAlarmMillis", nextAlarmMillis)
                }
            }
    }
}
