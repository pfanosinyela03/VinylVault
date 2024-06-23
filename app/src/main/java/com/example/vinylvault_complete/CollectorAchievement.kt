package com.example.vinylvault_complete

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class CollectorAchievement : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector_achievement)

        val v1Button = findViewById<Button>(R.id.button)

        v1Button.setOnClickListener {
            val intent = Intent(this, AddVinylActivity::class.java)
            startActivity(intent)
        }
    }
}