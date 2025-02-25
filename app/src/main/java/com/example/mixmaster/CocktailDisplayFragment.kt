package com.example.mixmaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mixmaster.R
import com.example.mixmaster.databinding.FragmentCocktailDisplayBinding

class CocktailDisplayFragment : Fragment() {

    private var _binding: FragmentCocktailDisplayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCocktailDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // קבלת הנתונים מה-arguments
        val cocktailName = arguments?.getString("cocktailName") ?: "Unknown Cocktail"
        val cocktailDescription = arguments?.getString("cocktailDescription") ?: "No description available."
        val cocktailIngredients = arguments?.getString("cocktailIngredients") ?: "No ingredients listed."
        val cocktailInstructions = arguments?.getString("cocktailInstructions") ?: "No instructions available."
        val cocktailImage = arguments?.getString("cocktailImage") ?: ""

        // הצגת הנתונים במסך
        binding.cocktailName.text = cocktailName
        binding.cocktailDescription.text = cocktailDescription
        binding.cocktailIngredients.text = cocktailIngredients
        binding.cocktailInstructions.text = cocktailInstructions

        // טעינת תמונה עם Glide
        Glide.with(this)
            .load(cocktailImage)
            .placeholder(R.drawable.ic_cocktail) // תמונה ברירת מחדל במקרה שאין כתובת תקפה
            .into(binding.cocktailImage)

        // כפתור חזרה
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // כפתור מחיקה (ניתן להוסיף פעולה למחיקת הנתונים או הצגת דיאלוג אישור)
        binding.deleteButton.setOnClickListener {
            // לדוגמה: Toast.makeText(requireContext(), "Cocktail deleted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
