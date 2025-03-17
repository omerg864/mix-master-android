package com.example.mixmaster

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.mixmaster.databinding.FragmentSettingsBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.viewModel.AuthViewModel

class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()

    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var didSetProfileImage = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            Log.d("SignUpFragment", "Image selected: $uri")
            // Set the selected image into the ImageView
            binding?.signUpImageView?.setImageURI(uri)
            didSetProfileImage = true
            // Optionally, store the URI to use it later for uploading
        } else {
            Log.d("SignUpFragment", "No image selected")
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
            // Launch gallery picker for images
            pickImageLauncher.launch("image/*")
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            binding?.signUpImageView?.setImageBitmap(bitmap)
            didSetProfileImage = true
        }

        binding?.buttonOpenCamera?.setOnClickListener {
            cameraLauncher?.launch(null)
        }

        binding?.updateProfileButton?.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun loadUserProfile() {
        authViewModel.user.value?.uid?.let { userId ->
            Log.d("SettingsFragment", "Loading profile for user: $userId")
            Model.shared.getUser(userId) { user ->
                activity?.runOnUiThread {
                    user?.let {
                        Log.d("SettingsFragment", "User data retrieved: $user")

                        binding?.userNameInput?.setText(it.name ?: "")
                        binding?.bioInputLayout?.editText?.setText(it.bio ?: "")

                        if (!it.image.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(it.image)
                                .placeholder(R.drawable.cocktails)
                                .error(R.drawable.cocktails)
                                .into(binding!!.signUpImageView)
                        } else {
                            Log.d("SettingsFragment", "User has no profile image")
                        }

                    } ?: Log.e("SettingsFragment", "User data is null!")
                }
            }
        } ?: Log.e("SettingsFragment", "User ID is null, cannot load profile.")
    }
    

    private fun updateUserProfile() {
        val newName = binding?.userNameInput?.text?.toString()
        if (newName.isNullOrEmpty()) {
            binding?.userNameInput?.error = "Please enter a name"
            return
        }
        val newBio = binding?.bioInputLayout?.editText?.text?.toString() ?: ""
        val userId = authViewModel.user.value?.uid ?: return
        val bitmap = (binding?.signUpImageView?.drawable as BitmapDrawable).bitmap

        Log.d("SettingsFragment", "Updating profile for user: $userId")
        Log.d("SettingsFragment", "New Name: $newName, New Bio: $newBio")

        binding?.progressBar?.visibility = View.VISIBLE
        binding?.form?.visibility = View.GONE
        if (!didSetProfileImage) {
            Log.d("SettingsFragment", "No profile image selected, skipping upload")
            Model.shared.updateUserProfile(authViewModel.user.value!!, newName, newBio, null) {
                if (it) {
                    Log.d("SettingsFragment", "Profile updated successfully")
                } else {
                    Log.e("SettingsFragment", "Failed to update profile")
                }
                binding?.progressBar?.visibility = View.GONE
                binding?.form?.visibility = View.VISIBLE
            }
        }
        else {
            Model.shared.updateUserProfile(authViewModel.user.value!!, newName, newBio, bitmap) {
                if (it) {
                    Log.d("SettingsFragment", "Profile updated successfully")
                } else {
                    Log.e("SettingsFragment", "Failed to update profile")
                }
                binding?.progressBar?.visibility = View.GONE
                binding?.form?.visibility = View.VISIBLE
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
