package com.example.mixmaster.adapter

import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmaster.R
import com.example.mixmaster.model.Cocktail
import com.google.android.material.button.MaterialButton
import com.bumptech.glide.Glide



interface OnItemClickListener {
    fun onItemClick(cocktail: Cocktail?)
}


class CocktailViewHolder(
    itemView: View,
    listener: OnItemClickListener?
): RecyclerView.ViewHolder(itemView) {

    private val imageView: ImageView = itemView.findViewById(R.id.cocktailImage)
    private val nameText: TextView = itemView.findViewById(R.id.cocktailName)
    private val descriptionText: TextView = itemView.findViewById(R.id.cocktailDescription)
    private val viewDetailsButton: MaterialButton = itemView.findViewById(R.id.viewDetailsButton)
    private var cocktail: Cocktail? = null

    init {


        viewDetailsButton.setOnClickListener {
            Log.d("TAG", "On click listener on position $adapterPosition")
            listener?.onItemClick(cocktail)
        }
    }

    fun bind(cocktail: Cocktail?, position: Int) {
        nameText.text = cocktail?.name
        descriptionText.text = cocktail?.description
        this.cocktail = cocktail

        Glide.with(itemView.context)
            .load(cocktail?.imageUrl)
            .centerCrop()
            .into(imageView)
    }
}
