package com.example.vinylvault_complete

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.vinylvault_complete.databinding.ActivityAddVinylBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddVinylActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVinylBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var categoryNames: MutableList<String> = mutableListOf()
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVinylBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Realtime Database and Storage references
        databaseReference = FirebaseDatabase.getInstance().reference.child("categories")
        storageReference = FirebaseStorage.getInstance().reference.child("vinyl_images")

        // Load categories into spinner
        loadCategoriesIntoSpinner()

        // Handle Choose Image button click
        binding.chooseImageButton.setOnClickListener {
            openImagePicker()
        }

        // Handle Add Vinyl button click
        binding.addVinylButton.setOnClickListener {
            val categoryName = binding.categorySpinner.selectedItem?.toString()
            val vinylName = binding.editTextVinylName.text.toString()
            val description = binding.editTextDescription.text.toString()
            val dateOfPurchase = binding.editTextDateOfPurchase.text.toString()

            if (categoryName.isNullOrEmpty() || vinylName.isEmpty() || description.isEmpty() || dateOfPurchase.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Please fill in all fields and choose an image", Toast.LENGTH_SHORT).show()
            } else {
                uploadImageAndSaveData(categoryName, vinylName, description, dateOfPurchase)
            }
        }
    }

    private fun loadCategoriesIntoSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryNames.clear()
                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.child("name").getValue(String::class.java)
                    categoryName?.let { categoryNames.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddVinylActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.imageViewAlbumArt.setImageURI(selectedImageUri)
            }
        }
    }

    private fun uploadImageAndSaveData(categoryName: String, vinylName: String, description: String, dateOfPurchase: String) {
        // Generate a unique filename for the image
        val imageFileName = "${UUID.randomUUID()}_${System.currentTimeMillis()}.jpg"
        val imageRef = storageReference.child(imageFileName)

        selectedImageUri?.let { uri ->
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                // Image uploaded successfully, get its download URL
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Save vinyl data along with image URL to Firebase Realtime Database
                    addVinylToCategory(categoryName, vinylName, description, dateOfPurchase, downloadUrl.toString())
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addVinylToCategory(categoryName: String, vinylName: String, description: String, dateOfPurchase: String, imageUrl: String) {
        val vinylData = mapOf(
            "name" to vinylName,
            "description" to description,
            "dateOfPurchase" to dateOfPurchase,
            "imageUrl" to imageUrl
        )

        // Save vinyl data to Firebase Realtime Database with vinylName as the key
        databaseReference.child(categoryName).child("vinyls").child(vinylName).setValue(vinylData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Vinyl added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add vinyl", Toast.LENGTH_SHORT).show()
                }
            }
    }

}

