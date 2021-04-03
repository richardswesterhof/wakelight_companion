package com.richardswesterhof.wakelightcompanion.utils

import android.content.Context
import android.content.SharedPreferences
import com.richardswesterhof.wakelightcompanion.R

class IdManager {
    companion object {
        private val notifIdPrefName: String = "nextNotificationId"

        /**
         * gets the next notification id that should be used
         * using this method ensures that the id will be increased by one,
         * so calling this method is all that is necessary
         */
        fun getNextNotifId(context: Context): Int {
            val sharedPref: SharedPreferences = context.getSharedPreferences(context.resources.getString(R.string.preference_file_store_internal_vars), Context.MODE_PRIVATE)

            val retVal = sharedPref.getInt(notifIdPrefName, 0)
            with(sharedPref.edit()) {
                putInt(notifIdPrefName, retVal + 1)
                apply()
            }

            return retVal
        }
    }
}