package com.example.mixmaster

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mixmaster.adapter.OnPostClickListener
import com.example.mixmaster.adapter.PostListAdapter
import com.example.mixmaster.databinding.FragmentProfileBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post
import com.example.mixmaster.viewModel.AuthViewModel
import com.example.mixmaster.viewModel.PostViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PostViewModel
    private lateinit var adapter: PostListAdapter
    private val authViewModel: AuthViewModel by activityViewModels()

    // Flags to track when each request finishes
    private var postsLoaded = false
    private var userLoaded = false

    // This variable will hold the user ID to display.
    // It is set either from arguments or, if absent, from the logged-in user.
    private var profileUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileUserId = arguments?.getString("userId") ?: authViewModel.user.value?.uid ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PostViewModel::class.java]

        // Setup RecyclerView for posts.
        binding.profileRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        // Initialize adapter with an empty list.
        adapter = PostListAdapter(emptyList())
        adapter.listener = object : OnPostClickListener {
            override fun onItemClick(post: Post?) {
                Log.d("TAG", "Clicked post: ${post?.name}")
                val bundle = Bundle()
                bundle.putString("postID", post?.id)
                findNavController().navigate(R.id.action_profileFragment_to_postDisplayFragment, bundle)
            }
        }
        binding.profileRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        // Reset flags
        postsLoaded = false
        userLoaded = false
        // Hide the main content and show the progress bar
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE

        getAllPosts()
        getUserDetails()
    }

    private fun checkIfAllRequestsFinished() {
        if (postsLoaded && userLoaded) {
            // Hide the progress bar and show the main content once all data is loaded
            binding.progressBar.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
        }
    }

    private fun getUserDetails() {
        if (profileUserId.isNotEmpty()) {
            Model.shared.getUser(profileUserId) { userObj ->
                activity?.runOnUiThread {
                    binding.userName.text = userObj?.name ?: "Unknown"
                    binding.userBio.text = userObj?.bio ?: ""
                    if (!userObj?.image.isNullOrEmpty()) {
                        Glide.with(this).load(userObj?.image).into(binding.profileImage)
                    }
                    userLoaded = true
                    checkIfAllRequestsFinished()
                }
            }
        } else {
            userLoaded = true
            checkIfAllRequestsFinished()
        }
    }

    private fun getAllPosts() {
        Model.shared.getAllUserPosts(profileUserId) { posts ->
            activity?.runOnUiThread {
                viewModel.set(posts = posts)
                binding.cocktailCount.text = posts.size.toString()
                adapter.set(posts)
                adapter.notifyDataSetChanged()
                postsLoaded = true
                checkIfAllRequestsFinished()
            }
        }
    }
}