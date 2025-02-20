package com.example.mixmaster

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mixmaster.adapter.CocktailListAdapter
import com.example.mixmaster.adapter.OnItemClickListener
import com.example.mixmaster.databinding.FragmentCocktailsBinding
import com.example.mixmaster.model.Post
import com.example.mixmaster.restAPI.RetrofitClient
import kotlinx.coroutines.launch

class CocktailsFragment : Fragment() {

    private var _binding: FragmentCocktailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CocktailListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCocktailsBinding.inflate(inflater, container, false)

        // Setup RecyclerView.
        binding.cocktailsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        // Initialize the adapter with an empty list.
        adapter = CocktailListAdapter(emptyList())
        adapter.listener = object : OnItemClickListener {
            override fun onItemClick(post: Post?) {
                Log.d("CocktailsFragment", "Clicked on cocktail: ${post?.name}")
                // You can handle navigation or other actions here.
            }
        }
        binding.cocktailsRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Fetch random cocktails when the fragment resumes.
        fetchRandomCocktails()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchRandomCocktails() {
        // Show progress bar before sending the request.
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getRandomCocktails()
                if (response.isSuccessful) {
                    val randomResponse = response.body()
                    val cocktails: List<Post> = randomResponse?.cocktails ?: emptyList()
                    // Update adapter's data and refresh the RecyclerView.
                    adapter.set(cocktails)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("CocktailsFragment", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("CocktailsFragment", "Exception fetching random cocktails", e)
            } finally {
                // Hide progress bar when request finishes.
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}