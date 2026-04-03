package com.example.foodhub

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var etCartAddress: TextInputEditText
    private lateinit var tilCartAddress: TextInputLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        etCartAddress = findViewById(R.id.etCartAddress)
        tilCartAddress = findViewById(R.id.tilCartAddress)

        // Load saved address from profile as default
        val sharedPref = getSharedPreferences("FoodHubPrefs", Context.MODE_PRIVATE)
        etCartAddress.setText(sharedPref.getString("address", ""))

        tilCartAddress.setEndIconOnClickListener {
            checkLocationPermission()
        }

        findViewById<MaterialButton>(R.id.btnCartPlaceOrder).setOnClickListener {
            val address = etCartAddress.text.toString()
            if (address.isNotEmpty()) {
                showOrderConfirmationDialog(address)
            } else {
                Toast.makeText(this, "Please provide a delivery address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    etCartAddress.setText(addresses[0].getAddressLine(0))
                    Toast.makeText(this, "Location detected", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Unable to find location. Turn on GPS.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showOrderConfirmationDialog(address: String) {
        val sharedPref = getSharedPreferences("FoodHubPrefs", Context.MODE_PRIVATE)
        val userPhone = sharedPref.getString("phone", "") ?: ""

        if (userPhone.isEmpty()) {
            Toast.makeText(this, "Please set your phone number in Profile first", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Order")
            .setMessage("Deliver to: $address\nTotal: ₹189")
            .setPositiveButton("Place Order") { _, _ ->
                // Start background SMS service with user's phone number
                val intent = Intent(this, OrderSMSService::class.java)
                intent.putExtra("PHONE_NUMBER", userPhone)
                intent.putExtra("MESSAGE", "FoodHub: Order confirmed! Delivering to $address")
                startService(intent)

                NotificationHelper.showNotification(this, "Order Confirmed", "Your food is being prepared!")
                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }
}
