package com.richardswesterhof.wakelightcompanion.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

abstract class ExtendedBroadcastReceiver(listeningFors: List<String>) : BroadcastReceiver() {

    private val ACTIONS_LISTENING_FOR: List<String> = listeningFors

    override fun onReceive(context: Context, intent: Intent) {
        val receivedAction: String? = intent.action
        if (!ACTIONS_LISTENING_FOR.contains(receivedAction)) {
            Log.e(this::class.simpleName, "Bad action: $receivedAction")
            return
        }

        trigger(context, intent)
    }

    /**
     * this function should be the one to be overridden by the implementing classes.
     * It's basically onReceive except a check will be performed if the action of the intent
     * matches the actions we are listening for before this function is called.
     * Thus it will be safe to assume that the intent is genuine
     */
    abstract fun trigger(context: Context, intent: Intent)
}
