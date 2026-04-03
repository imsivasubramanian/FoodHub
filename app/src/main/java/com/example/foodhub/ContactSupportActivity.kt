package com.example.foodhub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class ContactSupportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_support)

        val etPhone = findViewById<EditText>(R.id.etSupportPhone)
        val etMessage = findViewById<EditText>(R.id.etSupportMessage)
        val btnSend = findViewById<Button>(R.id.btnSendSms)

        btnSend.setOnClickListener {
            val phone = etPhone.text.toString()
            val message = etMessage.text.toString()

            if (phone.isNotEmpty() && message.isNotEmpty()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
                } else {
                    sendSms(phone, message)
                }
            } else {
                Toast.makeText(this, "Please enter phone and message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendSms(phone: String, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
            Toast.makeText(this, "SMS Sent successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "SMS failed to send", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val etPhone = findViewById<EditText>(R.id.etSupportPhone)
            val etMessage = findViewById<EditText>(R.id.etSupportMessage)
            sendSms(etPhone.text.toString(), etMessage.text.toString())
        }
    }
}
