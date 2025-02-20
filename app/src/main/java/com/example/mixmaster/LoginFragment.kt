package com.example.mixmaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mixmaster.databinding.FragmentLoginBinding
import com.example.mixmaster.viewModel.AuthViewModel

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLoginBinding.inflate(inflater, container, false)

        val joinButton: Button? = binding?.joinButton;
        val loginButton: Button? = binding?.loginButton;

        joinButton?.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment2_to_registerFragment2)
        }

        loginButton?.setOnClickListener {
            val email = binding?.emailEditText?.text.toString().trim()
            val password = binding?.passwordEditText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in email and password", Toast.LENGTH_SHORT).show()
            } else {
                binding?.progressBar?.visibility = View.VISIBLE
                binding?.joinButton?.isEnabled = false
                binding?.loginButton?.isEnabled = false
                authViewModel.signIn(email, password)
            }
        }

        authViewModel.user.observe(viewLifecycleOwner, Observer { firebaseUser ->
            firebaseUser?.let {
                // User signed in successfully, start MainActivity
                binding?.progressBar?.visibility = View.GONE
                binding?.joinButton?.isEnabled = true
                binding?.loginButton?.isEnabled = true
                if (firebaseUser != null) {
                    Log.d("TAG", "USER2: $firebaseUser")
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        })

        // Observe error messages
        authViewModel.error.observe(viewLifecycleOwner, Observer { errorMsg ->
            binding?.progressBar?.visibility = View.GONE
            binding?.joinButton?.isEnabled = true
            binding?.loginButton?.isEnabled = true
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })


        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}