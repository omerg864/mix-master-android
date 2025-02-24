package com.example.mixmaster

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mixmaster.adapter.CocktailListAdapter
import com.example.mixmaster.adapter.OnItemClickListener
import com.example.mixmaster.databinding.FragmentSearchBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post
import com.example.mixmaster.viewModel.PostViewModel
import androidx.core.widget.addTextChangedListener

class SearchFragment : Fragment() {

    private var binding: FragmentSearchBinding? = null
    private lateinit var viewModel: PostViewModel
    private lateinit var adapter: CocktailListAdapter

    // Handler and Runnable for debouncing the search query.
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    // Delay of 500 milliseconds.
    private val SEARCH_DELAY = 500L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[PostViewModel::class.java]

        // Setup RecyclerView for posts.
        binding?.cocktailsRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2)
        }
        // Initialize adapter with an empty list.
        adapter = CocktailListAdapter(emptyList())
        adapter.listener = object : OnItemClickListener {
            override fun onItemClick(post: Post?) {
                Log.d("TAG", "On click listener on post: ${post?.name}")
            }
        }
        binding?.cocktailsRecyclerView?.adapter = adapter

        // Set up the search EditText listener with debounce.
        binding?.searchEdit?.addTextChangedListener { text ->
            // Remove any previously queued search runnables.
            searchRunnable?.let { searchHandler.removeCallbacks(it) }
            // Create a new Runnable to perform the search after a delay.
            searchRunnable = Runnable {
                val query = text?.toString() ?: ""
                if (query.isEmpty()) {
                    getLastFourPosts()
                } else {
                    binding?.progressBar?.visibility = View.VISIBLE
                    Model.shared.searchPosts(query) { posts ->
                        activity?.runOnUiThread {
                            viewModel.set(posts = posts)
                            adapter.set(posts)
                            adapter.notifyDataSetChanged()
                            binding?.progressBar?.visibility = View.GONE
                        }
                    }
                }
            }
            // Post the new runnable with the specified delay.
            searchHandler.postDelayed(searchRunnable!!, SEARCH_DELAY)
        }

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        // If no search query is present, load the last four posts.
        if (binding?.searchEdit?.text.isNullOrEmpty()) {
            getLastFourPosts()
        }
    }

    private fun getLastFourPosts() {
        binding?.progressBar?.visibility = View.VISIBLE
        Model.shared.getLastFourPosts { posts ->
            activity?.runOnUiThread {
                viewModel.set(posts = posts)
                adapter.set(posts)
                adapter.notifyDataSetChanged()
                binding?.progressBar?.visibility = View.GONE
            }
        }
    }
}