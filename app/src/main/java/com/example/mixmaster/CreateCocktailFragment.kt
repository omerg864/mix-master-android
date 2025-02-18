package com.example.mixmaster

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.mixmaster.databinding.FragmentCreateCocktailBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post
import java.util.UUID


class CreateCocktailFragment : Fragment() {

    private var cameraLauncher: ActivityResultLauncher<Void?>? = null

    private var binding: FragmentCreateCocktailBinding? = null
    private var didSetProfileImage = false

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
            image = "",
            description = binding?.descriptionInput?.text?.toString() ?: "",
        )

        binding?.form?.visibility = View.GONE
        binding?.progressBar?.visibility = View.VISIBLE

        if (didSetProfileImage) {
            binding?.imageView?.isDrawingCacheEnabled = true
            binding?.imageView?.buildDrawingCache()
            val bitmap = (binding?.imageView?.drawable as BitmapDrawable).bitmap
            Model.shared.addPost(post, bitmap, Model.Storage.CLOUDINARY) {
                binding?.progressBar?.visibility = View.GONE
                binding?.form?.visibility = View.VISIBLE
                Navigation.findNavController(view).navigate(R.id.action_createCocktailFragment_to_homeFragment)
            }
        } else {
            Model.shared.addPost(post, null, Model.Storage.CLOUDINARY) {
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