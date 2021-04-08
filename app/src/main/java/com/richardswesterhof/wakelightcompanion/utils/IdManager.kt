package com.richardswesterhof.wakelightcompanion.utils

import android.content.Context
import android.content.SharedPreferences
import com.richardswesterhof.wakelightcompanion.R

class IdManager {
    companion object {
        private const val notifIdPrefName: String = "nextNotificationId"

        /**
         * gets the next notification id that should be used
         * using this method ensures that the id will be increased by one,
         * so calling this method is all that is necessary
         */
        fun getNextNotifId(context: Context): Int {
            val sharedPref: SharedPreferences = context.getSharedPreferences(context.resources.getString(R.string.preference_file_store_internal_vars), Context.MODE_PRIVATE)
            val retVal = peekNextNotifId(context)
            with(sharedPref.edit()) {
                putInt(notifIdPrefName, retVal + 1)
                apply()
            }

            return retVal
        }


        /**
         * peeks at the next notification id
         * THE RETURN VALUE OF THIS FUNCTION SHOULD NOT BE USED TO CREATE NOTIFICATIONS,
         * UNLESS YOU MANUALLY INCREMENT THE VALUE IN SHAREDPREFERENCES,
         * but at that point, just use getNextNotifId, that function already does that automatically
         */
        private fun peekNextNotifId(context: Context): Int {
            val sharedPref: SharedPreferences = context.getSharedPreferences(context.resources.getString(R.string.preference_file_store_internal_vars), Context.MODE_PRIVATE)
            return sharedPref.getInt(notifIdPrefName, 0)
        }
    }
}