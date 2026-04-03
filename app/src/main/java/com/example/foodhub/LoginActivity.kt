package com.example.foodhub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val btnOtp = findViewById<MaterialButton>(R.id.btnOtp)

        btnOtp.setOnClickListener {
            val phone = etPhone.text.toString()
            if (phone.length == 10) {
                checkNotificationPermissionAndSendOtp(phone)
            } else {
                etPhone.error = "Enter valid 10-digit number"
            }
        }
    }

    private fun checkNotificationPermissionAndSendOtp(phone: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            } else {
                sendOtp(phone)
            }
        } else {
            sendOtp(phone)
        }
    }

    private fun sendOtp(phone: String) {
        playNotificationSound()
        val otp = (1000..9999).random().toString()
        NotificationHelper.showNotification(this, "Your OTP Code", "Your OTP for FoodHub is: $otp")
        Toast.makeText(this, "OTP Sent to $phone", Toast.LENGTH_SHORT).show()
        
        val intent = Intent(this, OtpActivity::class.java)
        intent.putExtra("SENT_OTP", otp) // Pass the generated OTP
        startActivity(intent)
    }

    private fun playNotificationSound() {
        try {
            val notificationUri: Uri = Settings.System.DEFAULT_NOTIFICATION_URI
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, notificationUri)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
            sendOtp(etPhone.text.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
