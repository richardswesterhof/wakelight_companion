package com.richardswesterhof.wakelightcompanion.broadcast_receivers

import android.content.Context
import android.content.Intent

private val listeningFors = listOf("android.intent.action.BOOT_COMPLETED")

class AlarmReSetter: ExtendedBroadcastReceiver(listeningFors) {

    override fun trigger(context: Context, intent: Intent) {
        TODO("Not yet implemented")
    }

}