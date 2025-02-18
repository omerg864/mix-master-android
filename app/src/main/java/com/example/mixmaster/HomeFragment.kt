package com.example.mixmaster

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmaster.adapter.OnPostClickListener
import com.example.mixmaster.adapter.PostListAdapter
import com.example.mixmaster.databinding.FragmentHomeBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post

class HomeFragment : Fragment() {



    private var binding: FragmentHomeBinding? = null
    private var viewModel: HomeFragmentViewModel? = null
    private var adapter: PostListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeFragmentViewModel::class.java]


        /*val posts = listOf(
            PostPreview(
                id = "1",
                name = "Margarita Bliss",
                authorName = "Samantha Carpenter",
                authorImage = "https://example.com/profile1.jpg",
                postTime = "1 min. ago",
                images = listOf("https://images.immediate.co.uk/production/volatile/sites/30/2022/06/Tequila-sunrise-fb8b3ab.jpg?quality=90&resize=556,505"),
                description = "Share your favorite cocktail recipes with friends",
                likes = 256,
                comments = 45
            ),
            PostPreview(
                id = "2",
                name = "Tropical Sunset",
                authorName = "Cocktail",
                authorImage = "https://example.com/profile2.jpg",
                postTime = "3 hours ago",
                images = listOf("https://images.immediate.co.uk/production/volatile/sites/30/2022/06/Tequila-sunrise-fb8b3ab.jpg?quality=90&resize=556,505"),
                description = "Indulge in the art of cocktail making",
                likes = 256,
                comments = 45
            )
        )*/

        val postList: RecyclerView? = binding?.homeRecyclerView;
        postList?.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        postList?.layoutManager = layoutManager

        adapter = PostListAdapter(viewModel?.posts)
        adapter!!.listener = object : OnPostClickListener {
            override fun onItemClick(post: Post?) {
                Log.d("TAG", "On click Activity listener on position ${post?.name}");
            }
        }
        postList?.adapter = adapter


        return binding?.root
    }

    /*fun convertPostsToPreviews(posts: List<Post>?): List<PostPreview> {
        return posts?.map { post ->
            PostPreview(
                id = post.id,
                name = post.name,
                authorName = post.authorName,
                authorImage = post.authorImage,
                postTime = post.postTime,
                images = listOf(post.image), // Wrapping single image in a list
                description = post.description,
                likes = post.likes.size, // Getting the count of likes
                comments = post.comments.size // Getting the count of comments
            )
        } ?: emptyList()
    }*/

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        getAllPosts()
    }

    private fun getAllPosts() {

        binding?.progressBar?.visibility = View.VISIBLE

        Model.shared.getAllPosts {
            viewModel?.set(posts = it)
            adapter?.set(it)
            adapter?.notifyDataSetChanged()

            binding?.progressBar?.visibility = View.GONE
        }
    }
}