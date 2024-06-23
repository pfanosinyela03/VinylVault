package com.example.vinylvault_complete

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class NewbieActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newbie)

        val v3Button = findViewById<Button>(R.id.button3)

        v3Button.setOnClickListener {
            val intent = Intent(this, AddVinylActivity::class.java)
            startActivity(intent)
        }
    }
}