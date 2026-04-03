package com.example.foodhub

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FoodHub.db"
        private const val DATABASE_VERSION = 2

        // User Table
        private const val TABLE_USER = "users"
        private const val COL_USER_ID = "id"
        private const val COL_USER_NAME = "name"
        private const val COL_USER_EMAIL = "email"
        private const val COL_USER_PASS = "password"

        // Food Table
        private const val TABLE_FOOD = "food"
        private const val COL_FOOD_ID = "id"
        private const val COL_FOOD_NAME = "name"
        private const val COL_FOOD_PRICE = "price"

        // Cart Table
        private const val TABLE_CART = "cart"
        private const val COL_CART_ID = "id"
        private const val COL_CART_FOOD_NAME = "food_name"
        private const val COL_CART_QUANTITY = "quantity"
        
        // Profile Table
        private const val TABLE_PROFILE = "user_profile"
        private const val COL_PROF_ID = "id"
        private const val COL_PROF_NAME = "name"
        private const val COL_PROF_PHONE = "phone"
        private const val COL_PROF_EMAIL = "email"
        private const val COL_PROF_ADDR = "address"
        private const val COL_PROF_IMG = "image_uri"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_USER ($COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_USER_NAME TEXT, $COL_USER_EMAIL TEXT, $COL_USER_PASS TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_FOOD ($COL_FOOD_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_FOOD_NAME TEXT, $COL_FOOD_PRICE TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_CART ($COL_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_CART_FOOD_NAME TEXT, $COL_CART_QUANTITY INTEGER)")
        db?.execSQL("CREATE TABLE $TABLE_PROFILE ($COL_PROF_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_PROF_NAME TEXT, $COL_PROF_PHONE TEXT, $COL_PROF_EMAIL TEXT, $COL_PROF_ADDR TEXT, $COL_PROF_IMG TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FOOD")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        onCreate(db)
    }

    // --- USER OPERATIONS ---
    fun addUser(name: String, email: String, pass: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_NAME, name)
            put(COL_USER_EMAIL, email)
            put(COL_USER_PASS, pass)
        }
        return db.insert(TABLE_USER, null, values)
    }

    fun getAllUsers(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USER", null)
    }

    // --- FOOD OPERATIONS ---
    fun addFood(name: String, price: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_FOOD_NAME, name)
            put(COL_FOOD_PRICE, price)
        }
        return db.insert(TABLE_FOOD, null, values)
    }

    fun getAllFood(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_FOOD", null)
    }

    // --- CART OPERATIONS ---
    fun addToCart(foodName: String, quantity: Int): Long {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CART WHERE $COL_CART_FOOD_NAME = ?", arrayOf(foodName))
        val result: Long
        if (cursor.moveToFirst()) {
            val currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_QUANTITY))
            result = updateCartQuantity(foodName, currentQty + quantity).toLong()
        } else {
            val values = ContentValues().apply {
                put(COL_CART_FOOD_NAME, foodName)
                put(COL_CART_QUANTITY, quantity)
            }
            result = db.insert(TABLE_CART, null, values)
        }
        cursor.close()
        return result
    }

    fun getCartItems(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_CART", null)
    }

    fun updateCartQuantity(foodName: String, quantity: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_CART_QUANTITY, quantity)
        }
        return db.update(TABLE_CART, values, "$COL_CART_FOOD_NAME = ?", arrayOf(foodName))
    }

    fun removeFromCart(foodName: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_CART, "$COL_CART_FOOD_NAME = ?", arrayOf(foodName))
    }

    // --- PROFILE OPERATIONS ---
    fun saveUserProfile(name: String, phone: String, email: String, address: String, imageUri: String?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_PROF_NAME, name)
            put(COL_PROF_PHONE, phone)
            put(COL_PROF_EMAIL, email)
            put(COL_PROF_ADDR, address)
            put(COL_PROF_IMG, imageUri)
        }
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PROFILE WHERE $COL_PROF_ID = 1", null)
        if (cursor.count > 0) {
            db.update(TABLE_PROFILE, values, "$COL_PROF_ID = 1", null)
        } else {
            values.put(COL_PROF_ID, 1)
            db.insert(TABLE_PROFILE, null, values)
        }
        cursor.close()
    }

    fun getUserProfile(): Map<String, String?>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PROFILE WHERE $COL_PROF_ID = 1", null)
        var userMap: MutableMap<String, String?>? = null
        if (cursor.moveToFirst()) {
            userMap = mutableMapOf()
            userMap["name"] = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROF_NAME))
            userMap["phone"] = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROF_PHONE))
            userMap["email"] = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROF_EMAIL))
            userMap["address"] = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROF_ADDR))
            userMap["imageUri"] = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROF_IMG))
        }
        cursor.close()
        return userMap
    }
}
