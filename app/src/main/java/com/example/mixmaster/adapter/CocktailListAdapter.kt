package com.example.mixmaster.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmaster.R
import com.example.mixmaster.model.Post

class CocktailListAdapter(private var posts: List<Post>?): RecyclerView.Adapter<CocktailViewHolder>() {

    var listener: OnItemClickListener? = null

    override fun getItemCount(): Int = posts?.size ?: 0

    fun set(posts: List<Post>?) {
        this.posts = posts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CocktailViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.cocktail_holder,
            parent,
            false
        )
        return CocktailViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: CocktailViewHolder, position: Int) {
        holder.bind(
            post = posts?.get(position),
            position = position
        )
    }
}