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
                    it.tag = selectedImageUri.toString()
                }
            }
        }

    private val openCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    binding?.signUpImageView?.setImageBitmap(it)
                    uploadImageToFirebase(it) { imageUrl ->
                        if (imageUrl != null) {
                            binding?.signUpImageView?.tag = imageUrl
                            Log.d("SettingsFragment", "Image uploaded successfully: $imageUrl")
                        } else {
                            Log.d("SettingsFragment", "Failed to upload image")
                        }
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setupUI()
        loadUserProfile()

        return binding!!.root
    }

    private fun setupUI() {
        binding?.logoutButton?.setOnClickListener {
            authViewModel.signOut()
        }

        binding?.buttonChoosePicture?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            openGalleryLauncher.launch(intent)
        }

        binding?.buttonOpenCamera?.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            openCameraLauncher.launch(intent)
        }

        binding?.updateProfileButton?.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun loadUserProfile() {
        authViewModel.user.value?.uid?.let { userId ->
            Model.shared.getUser(userId) { user ->
                activity?.runOnUiThread {
                    user?.let {
                        binding?.userNameInput?.setText(it.name ?: "")
                        binding?.bioEditText?.setText(it.bio ?: "")

                        if (!it.image.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(it.image)
                                .placeholder(R.drawable.ic_cocktail)
                                .error(R.drawable.ic_cocktail)
                                .into(binding!!.signUpImageView)
                        }
                    }
                }
            }
        }
    }

    private fun updateUserProfile() {
        val newName = binding?.userNameInput?.text?.toString()
        val newBio = binding?.bioEditText?.text?.toString()
        val userId = authViewModel.user.value?.uid ?: return
        val selectedImageUri = binding?.signUpImageView?.tag as? String

        Model.shared.updateUserProfile(userId, newName, newBio, selectedImageUri) { success ->
            Log.d("SettingsFragment", if (success) "Profile updated successfully!" else "Failed to update profile")
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
                callback(uri.toString())
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
