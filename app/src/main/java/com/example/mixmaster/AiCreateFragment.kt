package com.example.mixmaster

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mixmaster.databinding.FragmentAiCreateBinding
import com.example.mixmaster.model.AiRequest
import com.example.mixmaster.model.Post
import com.example.mixmaster.restAPI.AiCocktailResponse
import com.example.mixmaster.restAPI.RetrofitClient
import kotlinx.coroutines.launch

class AiCreateFragment : Fragment() {

    private var binding: FragmentAiCreateBinding? = null
    private var difficulty: String = ""
    private var language: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAiCreateBinding.inflate(inflater, container, false)
        binding?.generateButton?.setOnClickListener {
            getAiCocktail()
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("SpinnerDebug", "onViewCreated called")

        setupSpinner(binding?.difficultySpinner, R.array.difficulty_levels, "Difficulty")
        setupSpinner(binding?.languageSpinner, R.array.language_options, "Language")
    }

    private fun setupSpinner(spinner: Spinner?, arrayResId: Int, tag: String) {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            arrayResId,
            R.layout.spinner_item
        ).also { it.setDropDownViewResource(R.layout.spinner_dropdown_item) }

        spinner?.adapter = adapter

        spinner?.post {
            if (spinner.adapter != null && spinner.adapter.count > 0) {
                spinner.setSelection(0, false)
                Log.d("SpinnerDebug", "$tag default selection set to first item")
            } else {
                Log.e("SpinnerDebug", "$tag adapter is empty!")
            }
        }

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                Log.d("SpinnerSelection", "$tag selected: $selectedItem")
                when (tag) {
                    "Difficulty" -> difficulty = selectedItem
                    "Language" -> language = selectedItem
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("SpinnerSelection", "$tag nothing selected")
            }
        }
    }

    private fun getAiCocktail() {
        //binding?.progressBar?.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                Log.d("CocktailsFragment", "Fetching ai")
                val response = RetrofitClient.apiService.getAiCocktail(
                    request = AiRequest(
                        ingredients = binding?.ingredientsInput?.text.toString(),
                        difficulty = difficulty,
                        language = language
                    )
                )
                if (response.isSuccessful) {
                    val aiResponse = response.body()
                    val cocktail: Post = aiResponse?.cocktail ?: Post("", "", "", "")
                    Log.d("CocktailsFragment", "Cocktail fetched: $cocktail")
                    // go to create cocktail fragment
                    val bundle = Bundle()
                    bundle.putString("cocktailName", cocktail.name)
                    bundle.putString("cocktailDescription", cocktail.description)
                    bundle.putString("cocktailIngredients", cocktail.ingredients)
                    bundle.putString("cocktailInstructions", cocktail.instructions)
                    findNavController().navigate(R.id.action_aiCreateFragment_to_createCocktailFragment, bundle)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
