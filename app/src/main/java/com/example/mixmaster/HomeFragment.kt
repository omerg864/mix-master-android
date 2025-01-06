package com.example.mixmaster

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmaster.adapter.OnPostClickListener
import com.example.mixmaster.adapter.PostListAdapter
import com.example.mixmaster.databinding.FragmentHomeBinding
import com.example.mixmaster.model.Post


class HomeFragment : Fragment() {



    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)


        val posts = listOf(
            Post(
                id = 1,
                name = "Margarita Bliss",
                authorName = "Samantha Carpenter",
                authorImage = "https://example.com/profile1.jpg",
                postTime = "1 min. ago",
                images = listOf("https://images.immediate.co.uk/production/volatile/sites/30/2022/06/Tequila-sunrise-fb8b3ab.jpg?quality=90&resize=556,505"),
                description = "Share your favorite cocktail recipes with friends",
                likes = 256,
                comments = 45
            ),
            Post(
                id = 2,
                name = "Tropical Sunset",
                authorName = "Cocktail",
                authorImage = "https://example.com/profile2.jpg",
                postTime = "3 hours ago",
                images = listOf("https://images.immediate.co.uk/production/volatile/sites/30/2022/06/Tequila-sunrise-fb8b3ab.jpg?quality=90&resize=556,505"),
                description = "Indulge in the art of cocktail making",
                likes = 256,
                comments = 45
            )
        )

        val postList: RecyclerView? = binding?.homeRecyclerView;
        postList?.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        postList?.layoutManager = layoutManager

        val adapter = PostListAdapter(posts)
        adapter.listener = object : OnPostClickListener {
            override fun onItemClick(post: Post?) {
                Log.d("TAG", "On click Activity listener on position ${post?.name}");
            }
        }
        postList?.adapter = adapter


        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}