package com.example.foodhub

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

object DatabaseExporter {

    fun exportDatabase(context: Context) {
        try {
            // First, force database creation if it doesn't exist
            val dbHelper = DatabaseHelper(context)
            dbHelper.readableDatabase.close() 

            val dbFile: File = context.getDatabasePath("FoodHub.db")
            
            if (!dbFile.exists()) {
                Toast.makeText(context, "Database is empty. Please add some data first!", Toast.LENGTH_LONG).show()
                return
            }

            // Create a temp file in cache
            val backupFile = File(context.cacheDir, "FoodHub_Backup.db")
            
            // Standard File Copy logic
            val inputStream = FileInputStream(dbFile)
            val outputStream = FileOutputStream(backupFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Get URI using FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "com.example.foodhub.fileprovider",
                backupFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share Database File"))
            
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
