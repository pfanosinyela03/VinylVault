package com.example.vinylvault_complete

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainMenuActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference //databaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")

        // Set up the achievements button click listener
        val achievementsButton = findViewById<ImageButton>(R.id.achievementsButton)
        achievementsButton.setOnClickListener {
            checkVinylCountAndStartActivity()
        }

        // Retrieve the username from the Intent
        val username = intent.getStringExtra("USERNAME")

        // Find the TextView in your layout
        val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)

        // Set the welcome message
        welcomeTextView.text = if (username != null) "Welcome, $username" else "Welcome"


        val categoryButton = findViewById<ImageButton>(R.id.menucategoryButton)

        categoryButton.setOnClickListener {
            val intent = Intent(this, CreateCategoryActivity::class.java)
            startActivity(intent)
        }

        val vinylButton = findViewById<ImageButton>(R.id.menuvinylButton)

        vinylButton.setOnClickListener {
            val intent = Intent(this, VinylDetailsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkVinylCountAndStartActivity() {

        val categoriesRef = FirebaseDatabase.getInstance().reference.child("categories")

        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalVinylCount = 0

                // Loop through each category to count the total number of vinyls
                for (categorySnapshot in snapshot.children) {
                    if (categorySnapshot.child("vinyls").exists()) {
                        // Count the number of vinyls in each category
                        val vinylCount = categorySnapshot.child("vinyls").childrenCount.toInt()
                        totalVinylCount += vinylCount
                    }
                }

                // Decide which activity to start based on the total vinyl count
                if (totalVinylCount == 0) {
                    // Navigate to NewbieActivity if there are zero vinyls
                    val intent = Intent(this@MainMenuActivity, NewbieActivity::class.java)
                    startActivity(intent)
                } else if (totalVinylCount < 3) {
                    // Navigate to StarterAchievement if vinyl count is between 1 and 2
                    val intent = Intent(this@MainMenuActivity, StarterAchievement::class.java)
                    startActivity(intent)
                } else if (totalVinylCount < 10) {
                    // Navigate to CollectorAchievement if vinyl count is between 3 and 9
                    val intent = Intent(this@MainMenuActivity, CollectorAchievement::class.java)
                    startActivity(intent)
                } else {
                    // Navigate to PackratAchievement if vinyl count is 10 or more
                    val intent = Intent(this@MainMenuActivity, PackratAchievement::class.java)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Error: ${error.message}")
                // Handle database error
            }
        })
    }


}
