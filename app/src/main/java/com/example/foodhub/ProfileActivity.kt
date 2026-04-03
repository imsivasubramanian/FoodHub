package com.example.foodhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfileImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvName = findViewById(R.id.tvProfileName)
        tvPhone = findViewById(R.id.tvProfilePhone)
        tvEmail = findViewById(R.id.tvProfileEmail)
        tvAddress = findViewById(R.id.tvProfileAddress)

        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        val user = dbHelper.getUserProfile()
        
        if (user != null) {
            tvName.text = user["name"] ?: "Guest User"
            tvPhone.text = user["phone"] ?: "Not Provided"
            tvEmail.text = user["email"] ?: "Not Provided"
            tvAddress.text = user["address"] ?: "Not Provided"

            val imageUriString = user["imageUri"]
            if (!imageUriString.isNullOrEmpty()) {
                try {
                    ivProfileImage.setImageURI(Uri.parse(imageUriString))
                } catch (e: Exception) {
                    ivProfileImage.setImageResource(R.drawable.logo2)
                }
            } else {
                ivProfileImage.setImageResource(R.drawable.logo2)
            }
        } else {
            // Default values if DB is empty
            tvName.text = "Guest User"
            tvPhone.text = "Not Provided"
            tvEmail.text = "Not Provided"
            tvAddress.text = "Not Provided"
            ivProfileImage.setImageResource(R.drawable.logo2)
        }
    }
}
