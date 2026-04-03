package com.example.foodhub

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView

class ShopActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<MaterialCardView>(R.id.shop1).setOnClickListener { openFoodCourt("Burger Point") }
        findViewById<MaterialCardView>(R.id.shop2).setOnClickListener { openFoodCourt("Fresh Juice Corner") }
        findViewById<MaterialCardView>(R.id.shop3).setOnClickListener { openFoodCourt("South Indian Mess") }
        findViewById<MaterialCardView>(R.id.shop4).setOnClickListener { openFoodCourt("Briyani House") }
    }

    private fun openFoodCourt(shopName: String) {
        val intent = Intent(this, FoodCourtActivity::class.java)
        intent.putExtra("SHOP_NAME", shopName)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_contact_support -> {
                startActivity(Intent(this, ContactSupportActivity::class.java))
                true
            }
            R.id.action_about_us -> {
                startActivity(Intent(this, AboutUsActivity::class.java))
                true
            }
            R.id.action_team_details -> {
                startActivity(Intent(this, TeamDetailsActivity::class.java))
                true
            }
            R.id.action_project_description -> {
                startActivity(Intent(this, ProjectDescriptionActivity::class.java))
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
