package com.example.mixmaster

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mixmaster.databinding.FragmentCocktailDisplayBinding
import com.example.mixmaster.model.AiRequest
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post
import com.example.mixmaster.restAPI.RetrofitClient
import kotlinx.coroutines.launch

class CocktailDisplayFragment : Fragment() {

    private var binding: FragmentCocktailDisplayBinding? = null
    private var cocktailId: String? = ""
    private var cocktail: Post? = null;


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCocktailDisplayBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cocktailId = arguments?.getString("cocktailId")
        getCocktail()
        binding?.forkButton?.setOnClickListener {
            val bundle = Bundle();
            bundle.putString("cocktailId", cocktailId);
            findNavController().navigate(R.id.action_cocktailDisplayFragment_to_createCocktailFragment, bundle);
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    private fun getCocktail() {
        if (cocktailId == null)
            return
        //binding?.progressBar?.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                Log.d("CocktailsFragment", "Fetching ai")
                val response = RetrofitClient.apiService.getCocktailById(cocktailId!!)
                if (response.isSuccessful) {
                    val cocktailResponse = response.body()
                    val newCocktail: Post = cocktailResponse?.cocktail ?: Post("", "", "", "")
                    Log.d("CocktailsFragment", "Cocktail fetched: $newCocktail")
                    // go to create cocktail fragment
                    cocktail = newCocktail

                    // הצגת הנתונים במסך
                    binding?.cocktailName?.text = cocktail?.name
                    binding?.cocktailDescription?.text = cocktail?.description
                    binding?.cocktailIngredients?.text = cocktail?.ingredients
                    binding?.cocktailInstructions?.text = cocktail?.instructions

                    // טעינת תמונה עם Glide
                    Glide.with(this@CocktailDisplayFragment)
                        .load(cocktail?.image)
                        .into(binding?.cocktailImage ?: return@launch)

                } else {
                    Log.e("CocktailsFragment", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("CocktailsFragment", "Exception fetching random cocktails", e)
            } finally {
                // Hide progress bar when request finishes.
                //binding?.progressBar?.visibility = View.GONE
            }
        }
    }

}
