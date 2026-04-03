package com.example.foodhub

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class OtpActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        val etOtp = findViewById<TextInputEditText>(R.id.etOtp)
        val btnVerify = findViewById<MaterialButton>(R.id.btnVerify)
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)

        // Receive the OTP sent from LoginActivity
        val sentOtp = intent.getStringExtra("SENT_OTP")

        btnBack.setOnClickListener {
            finish()
        }

        btnVerify.setOnClickListener {
            val enteredOtp = etOtp.text.toString()
            
            if (enteredOtp.isEmpty()) {
                etOtp.error = "Please enter OTP"
            } else if (enteredOtp == sentOtp) {
                playSuccessSound()
                showSuccessDialog()
            } else {
                etOtp.error = "Incorrect OTP. Please check your notification."
                Toast.makeText(this, "Verification Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playSuccessSound() {
        try {
            val notificationUri: Uri = Settings.System.DEFAULT_NOTIFICATION_URI
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, notificationUri)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("OTP Verified!")
            .setMessage("You have successfully verified your mobile number.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(this, ShopActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
