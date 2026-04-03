package com.example.foodhub

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FoodCourtActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foodcourt)

        dbHelper = DatabaseHelper(this)

        val shopName = intent.getStringExtra("SHOP_NAME") ?: "Food Court"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = shopName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val foodRecyclerView = findViewById<RecyclerView>(R.id.foodRecyclerView)
        val fabCart = findViewById<FloatingActionButton>(R.id.fabCart)

        // Populate Food table if empty
        checkAndPopulateFoodTable()

        // Fetch food list from SQLite
        val foodList = fetchFoodFromDatabase()

        foodRecyclerView.layoutManager = GridLayoutManager(this, 2)
        foodRecyclerView.adapter = FoodAdapter(foodList) { food ->
            // Save to Cart table in SQLite
            val id = dbHelper.addToCart(food.name, 1)
            if (id != -1L) {
                playSuccessSound()
                Toast.makeText(this, "${food.name} added to cart", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
            }
        }

        fabCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
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

    private fun checkAndPopulateFoodTable() {
        val cursor = dbHelper.getAllFood()
        if (cursor.count == 0) {
            dbHelper.addFood("Classic Burger", "₹149")
            dbHelper.addFood("Cheese Pizza", "₹299")
            dbHelper.addFood("Fried Chicken", "₹199")
            dbHelper.addFood("Iced Coffee", "₹120")
            dbHelper.addFood("Veg Sub", "₹159")
            dbHelper.addFood("Spicy Burger", "₹179")
        }
        cursor.close()
    }

    private fun fetchFoodFromDatabase(): List<Food> {
        val list = mutableListOf<Food>()
        val cursor = dbHelper.getAllFood()
        
        // Mapping DB rows to Food objects
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
                
                // Assigning static image resource based on name for demo
                val imageRes = when (name) {
                    "Classic Burger" -> R.drawable.img_1
                    "Cheese Pizza" -> R.drawable.img_10
                    "Fried Chicken" -> R.drawable.img_11
                    "Iced Coffee" -> R.drawable.img_9
                    "Veg Sub" -> R.drawable.img_12
                    else -> R.drawable.img_6
                }
                list.add(Food(name, price, imageRes))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
