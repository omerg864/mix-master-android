package com.example.mixmaster.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
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
        val post = posts?.get(position) // ✅ נגדיר את post לפני השימוש בו
        holder.bind(
            post = post,
            position = position
        )

        holder.itemView.setOnClickListener { view ->
            post?.let { //
                val bundle = Bundle().apply {
                    putString("cocktailName", it.name)
                    putString("cocktailDescription", it.description)
                    putString("cocktailIngredients", it.ingredients)
                    putString("cocktailInstructions", it.instructions)
                    putString("cocktailImage", post?.image ?: "")
                }
                view.findNavController().navigate(R.id.action_cocktailsFragment_to_cocktailDisplayFragment, bundle)
            }
        }
    }
}
