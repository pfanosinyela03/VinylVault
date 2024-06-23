package com.example.vinylvault_complete

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class PackratAchievement : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_packrat_achievement)

        val v2Button = findViewById<Button>(R.id.button2)

        v2Button.setOnClickListener {
            val intent = Intent(this, AddVinylActivity::class.java)
            startActivity(intent)
        }
    }
}