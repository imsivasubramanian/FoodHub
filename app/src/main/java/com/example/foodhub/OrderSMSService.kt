package com.example.foodhub

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast

class OrderSMSService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("PHONE_NUMBER")
        val message = intent?.getStringExtra("MESSAGE")

        if (phoneNumber != null && message != null) {
            sendSMS(phoneNumber, message)
        }

        return START_NOT_STICKY
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("OrderSMSService", "SMS sent successfully to $phoneNumber")
        } catch (e: Exception) {
            Log.e("OrderSMSService", "Failed to send SMS", e)
        }
        // Stop the service once the task is done
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
