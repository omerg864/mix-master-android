package com.example.mixmaster.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmaster.R
import com.example.mixmaster.model.Post

class PostListAdapter(private val posts: List<Post>): RecyclerView.Adapter<PostViewHolder>() {

    var listener: OnPostClickListener? = null

    override fun getItemCount(): Int = posts?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.post_holder,
            parent,
            false
        )
        return PostViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(
            post = posts?.get(position),
            position = position
        )
    }
}