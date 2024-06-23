package com.example.vinylvault_complete

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class VinylDetailsActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var categoryButton: Button
    private lateinit var vinylLayout: LinearLayout

    private lateinit var selectedCategoryTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vinyl_details)

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("categories")

        // Initialize views
        categoryButton = findViewById(R.id.categoryButton)
        vinylLayout = findViewById(R.id.vinylLayout)
        selectedCategoryTextView = findViewById(R.id.selectedCategoryTextView) // Add this line

        // Set up the button click listener to show categories
        categoryButton.setOnClickListener {
            showCategoryDialog()
        }
    }


    // Function to handle the click event of the "Add Vinyl" button
    fun addVinyl(view: View) {
        // Start the AddVinylActivity
        startActivity(Intent(this@VinylDetailsActivity, AddVinylActivity::class.java))
    }

    private fun showCategoryDialog() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<String>()
                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.key ?: continue
                    categories.add(categoryName)
                }

                // Create a dialog to display categories
                val builder = AlertDialog.Builder(this@VinylDetailsActivity)
                builder.setTitle("Select Category")

                val categoryArray = categories.toTypedArray()
                builder.setItems(categoryArray) { dialog, which ->
                    val selectedCategory = categoryArray[which]
                    displayVinyls(selectedCategory)

                    // Update the TextView with the selected category
                    selectedCategoryTextView.apply {
                        text = selectedCategory.toUpperCase() // Convert to uppercase
                        textSize = 24f // Set text size
                        gravity = Gravity.CENTER // Center text horizontally
                    }

                    dialog.dismiss()
                }

                builder.show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@VinylDetailsActivity,
                    "Failed to load categories",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }



    private fun displayVinyls(category: String) {
        vinylLayout.removeAllViews()

        databaseReference.child(category).child("vinyls")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("StringFormatInvalid")

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (vinylSnapshot in snapshot.children) {
                        val vinylName =
                            vinylSnapshot.child("name").getValue(String::class.java) ?: continue
                        val description =
                            vinylSnapshot.child("description").getValue(String::class.java)
                                ?: continue
                        val dateOfPurchase =
                            vinylSnapshot.child("dateOfPurchase").getValue(String::class.java)
                                ?: continue
                        val imageUrl = vinylSnapshot.child("imageUrl").getValue(String::class.java)

                        // Create a horizontal LinearLayout to hold each vinyl entry
                        val vinylEntryLayout = LinearLayout(this@VinylDetailsActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(0, 16, 0, 16)
                        }

                        // Create ImageView for the image
                        val imageView = ImageView(this@VinylDetailsActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                resources.getDimensionPixelSize(R.dimen.image_size),
                                resources.getDimensionPixelSize(R.dimen.image_size)
                            )
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setPadding(0, 0, 16, 0)
                        }

                        if (!imageUrl.isNullOrEmpty()) {
                            // Load the image using a library like Picasso or Glide
                            Glide.with(this@VinylDetailsActivity).load(imageUrl).into(imageView)
                        }

                        // Create a vertical LinearLayout to hold vinyl details
                        val detailsLayout = LinearLayout(this@VinylDetailsActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            orientation = LinearLayout.VERTICAL
                        }

                        // Create TextViews for vinyl details
                        val vinylNameTextView = TextView(this@VinylDetailsActivity).apply {
                            text = getString(R.string.vinyl_name_format, vinylName)
                            textSize = 21f
                            setTextColor(Color.BLACK)
                            setTypeface(null, Typeface.BOLD)
                        }

                        val descriptionTextView = TextView(this@VinylDetailsActivity).apply {
                            text = getString(R.string.description_format, description)
                            textSize = 18f
                            setTextColor(Color.BLACK)
                        }

                        val dateOfPurchaseTextView = TextView(this@VinylDetailsActivity).apply {
                            text = getString(R.string.date_of_purchase_format, dateOfPurchase)
                            textSize = 18f
                            setTextColor(Color.BLACK)
                        }

                        // Add TextViews to the detailsLayout
                        detailsLayout.addView(vinylNameTextView)
                        detailsLayout.addView(descriptionTextView)
                        detailsLayout.addView(dateOfPurchaseTextView)

                        // Add ImageView and detailsLayout to the vinylEntryLayout
                        vinylEntryLayout.addView(imageView)
                        vinylEntryLayout.addView(detailsLayout)

                        // Add the vinylEntryLayout to the main layout
                        vinylLayout.addView(vinylEntryLayout)

                        // Add a divider
                        val dividerView = View(this@VinylDetailsActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                resources.getDimensionPixelSize(R.dimen.divider_height)
                            )
                            setBackgroundColor(Color.BLACK)
                        }
                        vinylLayout.addView(dividerView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@VinylDetailsActivity,
                        "Failed to load vinyls",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // Function to show the delete vinyl dialog
    fun showDeleteDialog(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Vinyl")
        builder.setMessage("Enter the name of the vinyl you want to delete")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Delete") { dialog, which ->
            val vinylNameToDelete = input.text.toString()
            deleteVinyl(vinylNameToDelete)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    // Function to delete the vinyl
    private fun deleteVinyl(vinylName: String) {
        val selectedCategory =
            categoryButton.text.toString() // Get the selected category from the button text
        val vinylRef = databaseReference.child(selectedCategory).child("vinyls")

        vinylRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var vinylFound = false
                for (vinylSnapshot in snapshot.children) {
                    val name = vinylSnapshot.child("name").getValue(String::class.java)
                    if (name == vinylName) {
                        // Delete the associated image from Firebase Storage if exists
                        val imageUrl = vinylSnapshot.child("imageUrl").getValue(String::class.java)
                        if (!imageUrl.isNullOrEmpty()) {
                            val imageRef =
                                FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                            imageRef.delete().addOnSuccessListener {
                                // Image deleted successfully
                                // Proceed to delete the vinyl data from the database
                                vinylSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@VinylDetailsActivity,
                                            "Vinyl deleted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        displayVinyls(selectedCategory)
                                    } else {
                                        Toast.makeText(
                                            this@VinylDetailsActivity,
                                            "Failed to delete vinyl",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }.addOnFailureListener {
                                // Handle failure to delete image
                                Toast.makeText(
                                    this@VinylDetailsActivity,
                                    "Failed to delete vinyl image",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // No image associated, just delete the vinyl data
                            vinylSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@VinylDetailsActivity,
                                        "Vinyl deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    displayVinyls(selectedCategory)
                                } else {
                                    Toast.makeText(
                                        this@VinylDetailsActivity,
                                        "Failed to delete vinyl",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        vinylFound = true
                        break
                    }
                }
                if (!vinylFound) {
                    Toast.makeText(this@VinylDetailsActivity, "Vinyl not found", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@VinylDetailsActivity,
                    "Failed to delete vinyl",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}