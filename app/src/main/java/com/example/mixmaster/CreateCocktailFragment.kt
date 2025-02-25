package com.example.mixmaster

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.mixmaster.databinding.FragmentCreateCocktailBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post
import com.example.mixmaster.viewModel.AuthViewModel
import java.util.UUID


class CreateCocktailFragment : Fragment() {

    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private val authViewModel: AuthViewModel by activityViewModels()

    private var binding: FragmentCreateCocktailBinding? = null
    private var didSetProfileImage = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            Log.d("SignUpFragment", "Image selected: $uri")
            // Set the selected image into the ImageView
            binding?.imageView?.setImageURI(uri)
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

        binding = FragmentCreateCocktailBinding.inflate(inflater, container, false);

        val generateButton: Button? = binding?.generateButton

        generateButton?.setOnClickListener {
            findNavController().navigate(R.id.action_createCocktailFragment_to_aiCreateFragment)
        }

        binding?.publishButton?.setOnClickListener(::onSaveClicked)

        binding?.openImagesButton?.setOnClickListener {
            // Launch gallery picker for images
            pickImageLauncher.launch("image/*")
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            binding?.imageView?.setImageBitmap(bitmap)
            didSetProfileImage = true
        }

        binding?.takePictureButton?.setOnClickListener {
            cameraLauncher?.launch(null)
        }

        // Inflate the layout for this fragment
        return binding?.root
    }

    private fun onSaveClicked(view: View) {

        val post = Post(
            name = binding?.cocktailNameInput?.text?.toString() ?: "",
            id = UUID.randomUUID().toString(),
            description = binding?.descriptionInput?.text?.toString() ?: "",
            instructions = binding?.instructionsInput?.text?.toString() ?: "",
            ingredients = binding?.ingredientsInput?.text?.toString() ?: "",
            author = authViewModel.user.value?.uid.toString()
        )

        binding?.form?.visibility = View.GONE
        binding?.progressBar?.visibility = View.VISIBLE

        if (post.name.isEmpty() || post.description.isEmpty() || post.instructions.isEmpty() || post.ingredients.isEmpty()) {
            binding?.progressBar?.visibility = View.GONE
            binding?.form?.visibility = View.VISIBLE
            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_LONG).show()
            return
        }

        if (didSetProfileImage) {
            binding?.imageView?.isDrawingCacheEnabled = true
            binding?.imageView?.buildDrawingCache()
            val bitmap = (binding?.imageView?.drawable as BitmapDrawable).bitmap
            Model.shared.addPost(post, bitmap) {
                binding?.progressBar?.visibility = View.GONE
                binding?.form?.visibility = View.VISIBLE
                Navigation.findNavController(view).navigate(R.id.action_createCocktailFragment_to_homeFragment)
            }
        } else {
            Model.shared.addPost(post, null) {
                binding?.progressBar?.visibility = View.GONE
                binding?.form?.visibility = View.VISIBLE
                Navigation.findNavController(view).navigate(R.id.action_createCocktailFragment_to_homeFragment)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}