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
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeFragmentViewModel::class.java]


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