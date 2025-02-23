package com.example.mixmaster.adapter

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixmaster.R
import com.example.mixmaster.model.Post
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView


interface OnPostClickListener {
    fun onItemClick(post: Post?)
}


class PostViewHolder(
    itemView: View,
    listener: OnPostClickListener?
): RecyclerView.ViewHolder(itemView) {
    private var post: Post? = null

    private val postName: TextView = itemView.findViewById(R.id.postName)
    private val authorImage: CircleImageView = itemView.findViewById(R.id.authorImage)
    private val authorName: TextView = itemView.findViewById(R.id.authorName)
    private val postTime: TextView = itemView.findViewById(R.id.postTime)
    private val postImage: ImageView = itemView.findViewById(R.id.postImage)
    private val postDescription: TextView = itemView.findViewById(R.id.postDescription)
    private val likeButton: MaterialButton = itemView.findViewById(R.id.likeButton)
    private val commentButton: MaterialButton = itemView.findViewById(R.id.commentButton)
    private val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

    init {


        moreButton.setOnClickListener {
            // Handle more button click
            listener?.onItemClick(post)
        }

        likeButton.setOnClickListener {
            // Handle like button click
        }

        commentButton.setOnClickListener {
            // Handle comment button click
        }
    }

    fun bind(post: Post?, position: Int) {
        postName.text = post?.name
        authorName.text = post?.author
        postTime.text = ""
        postDescription.text = post?.description
        likeButton.text = "0"
        commentButton.text = "0"

        // Load images using Glide
        Glide.with(itemView.context)
            .load(post?.image)
            .placeholder(R.drawable.cocktails)
            .into(authorImage)
    }
}
