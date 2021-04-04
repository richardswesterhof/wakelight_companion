package com.richardswesterhof.wakelightcompanion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*


class MainFragment: Fragment() {

    private var dateMillis: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            dateMillis = it.getLong("nextAlarmMillis", 0)
        }

        replacePlaceholders(view, dateMillis)
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