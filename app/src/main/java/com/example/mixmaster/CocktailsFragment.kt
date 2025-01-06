package com.example.mixmaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmaster.adapter.CocktailListAdapter
import com.example.mixmaster.adapter.OnItemClickListener
import com.example.mixmaster.databinding.FragmentCocktailsBinding
import com.example.mixmaster.model.Cocktail

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CocktailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CocktailsFragment : Fragment() {


    private var binding: FragmentCocktailsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentCocktailsBinding.inflate(inflater, container, false)

        val cocktails = listOf(
            Cocktail(
                1,
                "Margarita Bliss",
                "A refreshing blend of tequila, lime, and triple sec served with a salted rim.",
                "https://images.immediate.co.uk/production/volatile/sites/30/2022/06/Tequila-sunrise-fb8b3ab.jpg?quality=90&resize=556,505"
            ),
            Cocktail(
                2,
                "Tropical Sunset",
                "Escape to paradise with a mix of rum, pineapple, and coconut flavors.",
                "https://assets.epicurious.com/photos/656f72061ce0aa7243171bbd/2:3/w_3168,h_4752,c_limit/Champagne-Cocktail_RECIPE_V1.jpg"
            ),
            Cocktail(
                3,
                "Classic Manhattan",
                "A sophisticated blend of whiskey, sweet vermouth, and bitters.",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT8mOBbQTa4p8g_-9yFPkcPA_YRIvdn-cqScQ&s"
            )
        )

        val cocktailList: RecyclerView? = binding?.cocktailsRecyclerView
        cocktailList?.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        cocktailList?.layoutManager = layoutManager

        val adapter = CocktailListAdapter(cocktails)
        adapter.listener = object : OnItemClickListener {
            override fun onItemClick(cocktail: Cocktail?) {
                Log.d("TAG", "On click Activity listener on position ${cocktail?.name}");
            }
        }
        cocktailList?.adapter = adapter

        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}