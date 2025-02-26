package com.example.mixmaster

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.mixmaster.databinding.FragmentSettingsBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.User
import com.example.mixmaster.viewModel.AuthViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream


class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()



    private val openGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                binding?.signUpImageView?.let {
                    Glide.with(requireContext()).load(selectedImageUri).into(it)
                    it.tag = selectedImageUri.toString() // שמירת ה-URI
                }
            }
        }


    private val openCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    binding?.signUpImageView?.setImageBitmap(imageBitmap)

                    // העלאת התמונה ל-Firebase Storage
                    uploadImageToFirebase(imageBitmap) { imageUrl ->
                        if (imageUrl != null) {
                            binding?.signUpImageView?.tag = imageUrl // שמירת ה-URI של התמונה
                            Log.d("TAG", "Image uploaded successfully: $imageUrl")
                        } else {
                            Log.d("TAG", "Failed to upload image")
                        }
                    }
                }
            }
        }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding?.logoutButton?.setOnClickListener {
            authViewModel.signOut()
        }

        binding?.buttonChoosePicture?.setOnClickListener {
            Log.d("TAG", "Choose Picture button clicked")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            openGalleryLauncher.launch(intent)
        }

        binding?.buttonOpenCamera?.setOnClickListener {
            Log.d("TAG", "Open Camera button clicked")
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            openCameraLauncher.launch(intent)
        }

        binding?.updateProfileButton?.setOnClickListener {
            val newName = binding?.userNameInput?.text?.toString()
            val userId = authViewModel.user.value?.uid ?: return@setOnClickListener

            // קבלת ה-URI של התמונה הנבחרת
            val selectedImageUri = binding?.signUpImageView?.tag as? String

            if (!newName.isNullOrEmpty() || !selectedImageUri.isNullOrEmpty()) {
                Model.shared.updateUserProfile(userId, newName, selectedImageUri) { success ->
                    if (success) {
                        Log.d("TAG", "Profile updated successfully!")
                    } else {
                        Log.d("TAG", "Failed to update profile")
                    }
                }
            }
        }


        loadUserProfile()

        return binding?.root
    }

    private fun loadUserProfile() {
        val user = authViewModel.user.value
        if (user != null) {
            Log.d("TAG", "User is signed in: ${user.uid}")
            Model.shared.getUser(user.uid) { userObj: User? ->
                activity?.runOnUiThread {
                    if (userObj != null) {
                        Log.d("TAG", "Fetched user: $userObj")

                        // עדכון שם המשתמש
                        binding?.userNameInput?.setText(userObj.name ?: "Unknown")

                        // טעינת תמונת הפרופיל אם קיימת
                        if (!userObj.image.isNullOrEmpty()) {
                            binding?.signUpImageView?.let {
                                Glide.with(requireContext())
                                    .load(userObj.image)
                                    .placeholder(R.drawable.ic_cocktail) // תמונה ברירת מחדל אם אין
                                    .error(R.drawable.ic_cocktail) // במקרה של שגיאה
                                    .into(it)
                            }
                        }

                    } else {
                        Log.d("TAG", "Failed to fetch user")
                    }
                }
            }
        }
    }
    private fun uploadImageToFirebase(imageBitmap: Bitmap, callback: (String?) -> Unit) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_images/${System.currentTimeMillis()}.jpg")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString()) // קבלת הקישור לתמונה ששמורה בפיירבייס
            }
        }.addOnFailureListener {
            callback(null) // כשלון בהעלאה
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
