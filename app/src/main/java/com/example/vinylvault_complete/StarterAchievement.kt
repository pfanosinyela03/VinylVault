package com.example.vinylvault_complete

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StarterAchievement : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starter_achievement)


        val v1Button = findViewById<Button>(R.id.button4)

        v1Button.setOnClickListener {
            val intent = Intent(this, AddVinylActivity::class.java)
            startActivity(intent)
        }



        // Initialize Firebase Realtime Database reference
        val currentUser = FirebaseAuth.getInstance().currentUser
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
            .child(currentUser?.uid ?: "")

        // Find the TextView in your layout
        val welcomeTextView: TextView = findViewById(R.id.textView3)

        // Fetch the username from the database
        if (currentUser != null) {
            databaseReference.child("username").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.getValue(String::class.java)
                    if (username != null) {
                        // Set the welcome message with the username
                        welcomeTextView.text = "Woop Woop, $username"
                    } else {
                        // Handle case where username is not found in the database
                        welcomeTextView.text = "Woop Woop"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                    welcomeTextView.text = "Woop Woop"
                }
            })
        } else {
            // Handle the case where the current user is not authenticated
            welcomeTextView.text = "Woop Woop"
        }
    }
}

