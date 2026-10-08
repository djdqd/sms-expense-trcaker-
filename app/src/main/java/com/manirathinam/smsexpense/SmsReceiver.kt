package com.manirathinam.smsexpense
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SmsReceiver: BroadcastReceiver() { override fun onReceive(context: Context, intent: Intent) { /* Future: persist parsed transactions locally. Current v1 rescans inbox on demand. */ } }
