package com.example.mixmaster

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmaster.adapter.CocktailListAdapter
import com.example.mixmaster.adapter.OnItemClickListener
import com.example.mixmaster.adapter.OnPostClickListener
import com.example.mixmaster.adapter.PostListAdapter
import com.example.mixmaster.adapter.onUserClickListener
import com.example.mixmaster.databinding.FragmentSearchBinding
import com.example.mixmaster.model.Cocktail
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post
import com.example.mixmaster.viewModel.PostViewModel


class SearchFragment : Fragment() {

    private var binding: FragmentSearchBinding? = null

    private lateinit var viewModel: PostViewModel
    private lateinit var adapter: CocktailListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchBinding.inflate(inflater, container, false);

        /*val cocktails = listOf(
            Cocktail(
                "1",
                "Margarita Bliss",
                "A refreshing blend of tequila, lime, and triple sec served with a salted rim.",
                "https://images.immediate.co.uk/production/volatile/sites/30/2022/06/Tequila-sunrise-fb8b3ab.jpg?quality=90&resize=556,505"
            ),
            Cocktail(
                "2",
                "Tropical Sunset",
                "Escape to paradise with a mix of rum, pineapple, and coconut flavors.",
                "https://assets.epicurious.com/photos/656f72061ce0aa7243171bbd/2:3/w_3168,h_4752,c_limit/Champagne-Cocktail_RECIPE_V1.jpg"
            ),
            Cocktail(
                "3",
                "Classic Manhattan",
                "A sophisticated blend of whiskey, sweet vermouth, and bitters.",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT8mOBbQTa4p8g_-9yFPkcPA_YRIvdn-cqScQ&s"
            )
        )

        val cocktailList: RecyclerView? = binding?.cocktailsRecyclerView
        cocktailList?.setHasFixedSize(true)

        cocktailList?.layoutManager = GridLayoutManager(context, 2)

        val adapter = CocktailListAdapter(cocktails)
        adapter.listener = object : OnItemClickListener {
            override fun onItemClick(cocktail: Cocktail?) {
                Log.d("TAG", "On click Activity listener on position ${cocktail?.name}");
            }
        }
        cocktailList?.adapter = adapter*/

        viewModel = ViewModelProvider(this)[PostViewModel::class.java]

        // Setup RecyclerView for posts.
        binding?.cocktailsRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2)
        }
        // Initialize adapter with an empty list
        adapter = CocktailListAdapter(emptyList())

        adapter.listener = object : OnItemClickListener {
            override fun onItemClick(post: Post?) {
                Log.d("TAG", "On click listener on post: ${post?.name}")
            }
        }

        binding?.cocktailsRecyclerView?.adapter = adapter

        return binding?.root

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        getLastFourPosts()
    }

    private fun getLastFourPosts() {
        Model.shared.getLastFourPosts { posts ->
            activity?.runOnUiThread {
                viewModel.set(posts = posts)
                adapter.set(posts)
                adapter.notifyDataSetChanged()
            }
        }
    }
}