package com.example.mixmaster.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixmaster.R
import com.example.mixmaster.model.Post
import com.example.mixmaster.utils.toFirebaseTimestamp
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale


interface OnPostClickListener {
    fun onItemClick(post: Post?)
}

interface onUserClickListener {
    fun onItemClick(id: String?)
}


class PostViewHolder(
    itemView: View,
    listener: OnPostClickListener?,
    authorListener: onUserClickListener?
): RecyclerView.ViewHolder(itemView) {
    private var post: Post? = null

    private val postName: TextView = itemView.findViewById(R.id.postName)
    private val authorImage: CircleImageView = itemView.findViewById(R.id.authorImage)
    private val authorName: TextView = itemView.findViewById(R.id.authorName)
    private val postTime: TextView = itemView.findViewById(R.id.postTime)
    private val postImage: ImageView = itemView.findViewById(R.id.postImage)
    private val postDescription: TextView = itemView.findViewById(R.id.postDescription)


    init {


        postImage.setOnClickListener {
            listener?.onItemClick(post)
        }

        authorName.setOnClickListener {
            authorListener?.onItemClick(post?.author)
        }

        authorImage.setOnClickListener {
            authorListener?.onItemClick(post?.author)
        }

    }

    fun bind(post: Post?, position: Int) {
        this.post = post
        postName.text = post?.name
        authorName.text = post?.authorName
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        val date = post?.createdAt?.toFirebaseTimestamp?.toDate()
        postTime.text = if (date != null) dateFormat.format(date) else ""
        postDescription.text = post?.description

        // Load images using Glide
        Glide.with(itemView.context)
            .load(post?.authorImage)
            .placeholder(R.drawable.cocktails)
            .into(authorImage)

        Glide.with(itemView.context)
            .load(post?.image)
            .placeholder(R.drawable.cocktails)
            .into(postImage)
    }
}
