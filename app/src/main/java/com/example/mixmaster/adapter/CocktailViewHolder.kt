package com.example.mixmaster.adapter

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmaster.R
import com.google.android.material.button.MaterialButton
import com.bumptech.glide.Glide
import com.example.mixmaster.model.Post


interface OnItemClickListener {
    fun onItemClick(post: Post?)
}


class CocktailViewHolder(
    itemView: View,
    listener: OnItemClickListener?
): RecyclerView.ViewHolder(itemView) {

    private val imageView: ImageView = itemView.findViewById(R.id.cocktailImage)
    private val nameText: TextView = itemView.findViewById(R.id.cocktailName)
    private val descriptionText: TextView = itemView.findViewById(R.id.cocktailDescription)
    private val viewDetailsButton: MaterialButton = itemView.findViewById(R.id.viewDetailsButton)
    private var post: Post? = null

    init {

        viewDetailsButton.setOnClickListener {
            listener?.onItemClick(post)
        }

    }

    fun bind(post: Post?, position: Int) {
        nameText.text = post?.name
        descriptionText.text = post?.description
        this.post = post

        if (post?.image == "") {
            return
        }
        Glide.with(itemView.context)
            .load(post?.image)
            .centerCrop()
            .into(imageView)
    }
}
