package com.example.mixmaster

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mixmaster.databinding.FragmentPostDisplayBinding
import com.example.mixmaster.databinding.FragmentRegisterBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post
import com.example.mixmaster.utils.toFirebaseTimestamp
import com.example.mixmaster.viewModel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Locale


class PostDisplayFragment : Fragment() {

    private var binding: FragmentPostDisplayBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()

    private var postID: String? = null
    private var post: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postID = it.getString("postID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPostDisplayBinding.inflate(inflater, container, false)

        getPost()

        binding?.EditButton?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("postID", postID)
            findNavController().navigate(R.id.action_postDisplayFragment_to_editPostFragment, bundle)
        }

        binding?.deleteButton?.setOnClickListener {
            Model.shared.deletePost(post!!) {
                if (it) {
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding?.authorImage?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("userId", post?.author)
            findNavController().navigate(R.id.action_postDisplayFragment_to_profileFragment, bundle)
        }

        binding?.authorName?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("userId", post?.author)
            findNavController().navigate(R.id.action_postDisplayFragment_to_profileFragment, bundle)
        }

        return binding?.root
    }

    private fun getPost() {
        if (postID == null) {
            return
        }
        Model.shared.getPostById(postID!!) { post ->
            this.post = post;
            activity?.runOnUiThread {
                binding?.cocktailName?.text = post?.name
                binding?.cocktailDescription?.text = post?.description
                binding?.cocktailIngredients?.text = post?.ingredients
                binding?.cocktailInstructions?.text = post?.instructions
                binding?.authorName?.text = post?.authorName
                val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
                val date = post?.createdAt?.toFirebaseTimestamp?.toDate()
                binding?.postTime?.text = if (date != null) dateFormat.format(date) else ""
                Glide.with(this).load(post?.authorImage).into(binding?.authorImage ?: return@runOnUiThread)

                if (authViewModel.user.value?.uid == post?.author) {
                    binding?.EditButton?.visibility = View.VISIBLE
                    binding?.deleteButton?.visibility = View.VISIBLE
                }


                if (post?.image != "") {
                    Glide.with(this).load(post?.image).into(binding?.cocktailImage ?: return@runOnUiThread)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}