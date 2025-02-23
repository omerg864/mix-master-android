package com.example.mixmaster

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mixmaster.adapter.OnPostClickListener
import com.example.mixmaster.adapter.PostListAdapter
import com.example.mixmaster.databinding.FragmentHomeBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post

class HomeFragment : Fragment() {

    // Use a backing property for binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var adapter: PostListAdapter
    // Use AuthViewModel only to obtain the user id.
    private val authViewModel: AuthViewModel by activityViewModels()

    // Flags to track when each request finishes
    private var postsLoaded = false
    private var userLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeFragmentViewModel::class.java]

        // Setup RecyclerView for posts.
        binding.homeRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        // Initialize adapter with an empty list
        adapter = PostListAdapter(emptyList())
        adapter.listener = object : OnPostClickListener {
            override fun onItemClick(post: Post?) {
                Log.d("TAG", "On click listener on post: ${post?.name}")
            }
        }
        binding.homeRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        // Reset flags and show the progress bar
        postsLoaded = false
        userLoaded = false
        binding.progressBar.visibility = View.VISIBLE

        getAllPosts()
        getUserDetails()
    }

    private fun checkIfAllRequestsFinished() {
        if (postsLoaded && userLoaded) {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun getUserDetails() {
        val user = authViewModel.user.value
        if (user != null) {
            Model.shared.getUser(user.uid) { userObj ->
                activity?.runOnUiThread {
                    Log.d("TAG", "Fetched user: $userObj")
                    binding.userNameTextView.text = userObj?.name ?: "Unknown"
                    if (userObj?.image != "") {
                        Glide.with(this).load(userObj?.image).into(binding.profileImage)
                    }
                    userLoaded = true
                    checkIfAllRequestsFinished()
                }
            }
        } else {
            Log.d("TAG", "User is not signed in")
            userLoaded = true
            checkIfAllRequestsFinished()
        }
    }

    private fun getAllPosts() {
        Model.shared.getAllPosts { posts ->
            activity?.runOnUiThread {
                viewModel.set(posts = posts)
                adapter.set(posts)
                adapter.notifyDataSetChanged()
                postsLoaded = true
                checkIfAllRequestsFinished()
            }
        }
    }
}