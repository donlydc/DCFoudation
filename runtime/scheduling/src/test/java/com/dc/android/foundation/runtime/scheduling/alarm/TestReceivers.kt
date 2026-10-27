package com.dc.android.foundation.runtime.scheduling.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Never instantiated: tests only need distinct, loadable receiver class names for the codec. */
internal class FirstTestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}

internal class SecondTestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
