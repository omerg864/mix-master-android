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
import android.widget.Toast
import com.example.mixmaster.databinding.FragmentAiCreateBinding


class AiCreateFragment : Fragment() {

    private var _binding: FragmentAiCreateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiCreateBinding.inflate(inflater, container, false)

        setupSpinner(binding.difficultySpinner, R.array.difficulty_levels, "Difficulty")
        setupSpinner(binding.languageSpinner, R.array.language_options, "Language")

        return binding.root
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int, tag: String) {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            arrayResId,
            R.layout.spinner_item
        ).also { it.setDropDownViewResource(R.layout.spinner_dropdown_item) }

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                Log.d("SpinnerSelection", "$tag selected: $selectedItem")
                Toast.makeText(requireContext(), "$tag: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("SpinnerSelection", "$tag nothing selected")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

