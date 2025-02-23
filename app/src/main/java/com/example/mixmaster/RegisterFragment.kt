package com.example.mixmaster

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mixmaster.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    var binding: FragmentRegisterBinding? = null
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
    ): View? {

        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        val loginText = binding?.loginText

        loginText?.setOnClickListener {
            findNavController().popBackStack()
        }
        binding?.registerButton?.setOnClickListener(::onRegisterButtonClick)

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

        authViewModel.user.observe(viewLifecycleOwner, { user ->
            binding?.progressBar?.visibility = View.GONE
            binding?.registerLayout?.visibility = View.VISIBLE
            if (user != null) {
                goToHomeFragment()
            }
        })

        authViewModel.error.observe(viewLifecycleOwner, { errorMsg ->
            binding?.progressBar?.visibility = View.GONE
            binding?.registerLayout?.visibility = View.VISIBLE
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        return binding?.root
    }

    fun goToHomeFragment() {
        val intent = Intent(requireContext(), MainActivity::class.java);
        startActivity(intent);
        requireActivity().finish();
    }

    fun onRegisterButtonClick(view: View) {
        val name = binding?.nameEditText?.text.toString();
        val email = binding?.emailEditText?.text.toString()
        val password1 = binding?.passwordEditText?.text.toString()
        val password2 = binding?.confirmPasswordEditText?.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty() && password1.isNotEmpty() && password2.isNotEmpty()) {
            if (password1 == password2) {
                binding?.progressBar?.visibility = View.VISIBLE
                binding?.registerLayout?.visibility = View.GONE
                if(didSetProfileImage) {
                    binding?.signUpImageView?.isDrawingCacheEnabled = true
                    binding?.signUpImageView?.buildDrawingCache()
                    val bitmap = (binding?.signUpImageView?.drawable as BitmapDrawable).bitmap
                    Log.d("TAG", "Bitmap: $bitmap")
                    authViewModel.signUp(email, password1, name, bitmap)
                } else {
                    Log.d("TAG", "No image selected")
                    authViewModel.signUp(email, password1, name, null)
                }
            } else {
                // Passwords don't match, display an error message
                Toast.makeText(
                    requireContext(), "Passwords don't match.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Fields are empty, display an error message
            Toast.makeText(
                requireContext(), "All fields are required.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}