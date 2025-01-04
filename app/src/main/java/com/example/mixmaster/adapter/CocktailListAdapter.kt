package com.example.mixmaster.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmaster.R
import com.example.mixmaster.model.Cocktail

class CocktailListAdapter(private val cocktails: List<Cocktail>): RecyclerView.Adapter<CocktailViewHolder>() {

    var listener: OnItemClickListener? = null

    override fun getItemCount(): Int = cocktails?.size ?: 0

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
            cocktail = cocktails?.get(position),
            position = position
        )
    }
}