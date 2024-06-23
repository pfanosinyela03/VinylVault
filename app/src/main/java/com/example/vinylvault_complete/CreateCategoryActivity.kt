package com.example.vinylvault_complete

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.vinylvault_complete.databinding.ActivityCreateCategoryBinding
import com.google.firebase.database.*

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateCategoryBinding
    private var categoryCount = 0
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("categories")

        // Load existing categories from Firebase
        loadCategories()

        // Handle "Add" button click
        binding.addButton.setOnClickListener {
            showAddCategoryDialog()
        }

        // Handle "Delete" button click
        binding.deleteButton.setOnClickListener {
            showDeleteCategoryDialog()
        }


    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val editTextCategoryName = dialogView.findViewById<EditText>(R.id.editTextCategoryName)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val categoryName = editTextCategoryName.text.toString()
                if (categoryName.isNotEmpty()) {
                    val color = generateRandomColor()
                    addCategory(categoryName, color)
                    saveCategory(categoryName, color)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
            setTextColor(Color.GREEN)
            textSize = 18f
            setPadding(40, 20, 40, 20)
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
            setTextColor(Color.RED)
            textSize = 18f
            setPadding(40, 20, 40, 20)
        }
    }

    private fun showDeleteCategoryDialog() {
        // Inflate the dialog_delete_category layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_category, null)
        val editTextCategoryName = dialogView.findViewById<EditText>(R.id.editTextCategoryNameToDelete)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Delete") { dialog, _ ->
                val categoryName = editTextCategoryName.text.toString()
                if (categoryName.isNotEmpty()) {
                    deleteCategory(categoryName)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
            setTextColor(Color.RED)
            textSize = 18f
            setPadding(40, 20, 40, 20)
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
            setTextColor(Color.GRAY)
            textSize = 18f
            setPadding(40, 20, 40, 20)
        }
    }

    private fun addCategory(categoryName: String, color: Int) {
        categoryCount++

        val categoryButton = Button(this).apply {
            text = categoryName
            setBackgroundColor(color)
            setTextColor(Color.BLACK)
            textSize = 20f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            setPadding(32, 24, 32, 24)
        }

        binding.categoryContainer.addView(categoryButton)

        // Set click listener to navigate to AddVinylActivity with category name
        categoryButton.setOnClickListener {
            val intent = Intent(this, AddVinylActivity::class.java)
            intent.putExtra("categoryName", categoryName)
            startActivity(intent)
        }
    }

    private fun deleteCategory(categoryName: String) {
        databaseReference.orderByChild("name").equalTo(categoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (categorySnapshot in dataSnapshot.children) {
                            // Remove the category from Firebase
                            categorySnapshot.ref.removeValue()
                        }
                        Toast.makeText(this@CreateCategoryActivity, "Category deleted successfully", Toast.LENGTH_SHORT).show()
                        // Remove the category button from the UI
                        removeCategoryFromUI(categoryName)
                    } else {
                        Toast.makeText(this@CreateCategoryActivity, "Category not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@CreateCategoryActivity, "Failed to delete category", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun removeCategoryFromUI(categoryName: String) {
        for (i in 0 until binding.categoryContainer.childCount) {
            val button = binding.categoryContainer.getChildAt(i) as Button
            if (button.text == categoryName) {
                binding.categoryContainer.removeViewAt(i)
                categoryCount--
                break
            }
        }
    }

    private fun generateRandomColor(): Int {
        val random = java.util.Random()
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

    private fun saveCategory(categoryName: String, color: Int) {
        val categoryData = mapOf(
            "name" to categoryName,
            "color" to color
        )

        // Save category data to Firebase Realtime Database with categoryName as the key
        databaseReference.child(categoryName).setValue(categoryData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Category saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save category", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun loadCategories() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.child("name").getValue(String::class.java)
                    val color = categorySnapshot.child("color").getValue(Int::class.java)
                    if (categoryName != null && color != null) {
                        addCategory(categoryName, color)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CreateCategoryActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
