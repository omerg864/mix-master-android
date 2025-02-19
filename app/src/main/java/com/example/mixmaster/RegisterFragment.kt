package com.example.mixmaster

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mixmaster.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    var binding: FragmentRegisterBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        val loginText = binding?.loginText

        loginText?.setOnClickListener {
            findNavController().popBackStack()
        }
        binding?.registerButton?.setOnClickListener(::onRegisterButtonClick)

        authViewModel.user.observe(viewLifecycleOwner, { user ->
            if (user != null) {
                goToHomeFragment()
            }
        })

        return binding?.root
    }

    fun goToHomeFragment() {
        val intent = Intent(requireContext(), MainActivity::class.java);
        startActivity(intent);
        requireActivity().finish();
    }

    fun onRegisterButtonClick(view: View) {
        val name = binding?.nameEditText?.text.toString();
        val email = binding?.emailEditText?.text.toString()
        val password1 = binding?.passwordEditText?.text.toString()
        val password2 = binding?.confirmPasswordEditText?.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty() && password1.isNotEmpty() && password2.isNotEmpty()) {
            if (password1 == password2) {
                authViewModel.signUp(email, password1, name)
            } else {
                // Passwords don't match, display an error message
                Toast.makeText(
                    requireContext(), "Passwords don't match.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Fields are empty, display an error message
            Toast.makeText(
                requireContext(), "All fields are required.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}