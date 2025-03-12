package com.example.mixmaster

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.mixmaster.databinding.FragmentPostDisplayBinding
import com.example.mixmaster.databinding.FragmentRegisterBinding
import com.example.mixmaster.model.Model


class PostDisplayFragment : Fragment() {

    private var binding: FragmentPostDisplayBinding? = null

    private var postID: String? = null

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

        return binding?.root
    }

    private fun getPost() {
        if (postID == null) {
            return
        }
        Model.shared.getPostById(postID!!) { post ->
            activity?.runOnUiThread {
                binding?.cocktailName?.text = post?.name
                binding?.cocktailDescription?.text = post?.description
                binding?.cocktailIngredients?.text = post?.ingredients
                binding?.cocktailInstructions?.text = post?.instructions

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