package com.example.foodhub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvLocation: TextView
    private lateinit var etManualAddress: EditText
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        tvLocation = findViewById(R.id.tvLocation)
        etManualAddress = findViewById(R.id.etManualAddress)

        findViewById<Button>(R.id.btnGetLocation).setOnClickListener {
            checkLocationPermission()
        }

        findViewById<Button>(R.id.btnPlaceOrder).setOnClickListener {
            val address = if (etManualAddress.text.isNotEmpty()) etManualAddress.text.toString() else tvLocation.text.toString()
            checkSMSPermissionAndPlaceOrder(address)
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.get(0)?.getAddressLine(0) ?: "Lat: ${location.latitude}, Lon: ${location.longitude}"
                tvLocation.text = "Delivery Address: $address"
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkSMSPermissionAndPlaceOrder(address: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
        } else {
            startSMSService(address)
        }
    }

    private fun startSMSService(address: String) {
        // Play success sound
        try {
            val notificationUri: Uri = Settings.System.DEFAULT_NOTIFICATION_URI
            mediaPlayer = MediaPlayer.create(this, notificationUri)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val intent = Intent(this, OrderSMSService::class.java)
        // Hardcoded for demo, usually you'd get this from user profile
        intent.putExtra("PHONE_NUMBER", "1234567890") 
        intent.putExtra("MESSAGE", "Order Confirmed! Delivering to: $address")
        startService(intent)
        
        Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
        // Notification for immediate feedback
        NotificationHelper.showNotification(this, "FoodHub", "Your order has been placed!")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val address = if (etManualAddress.text.isNotEmpty()) etManualAddress.text.toString() else tvLocation.text.toString()
            startSMSService(address)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
