package com.example.mixmaster

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mixmaster.model.Model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {

    private val model = Model.shared

    // LiveData holding the current FirebaseUser.
    private val _user = MutableLiveData<FirebaseUser?>().apply {
        value = FirebaseAuth.getInstance().currentUser
    }
    val user: LiveData<FirebaseUser?> = _user

    // LiveData holding the user's display name.
    private val _displayName = MutableLiveData<String?>()
    val displayName: LiveData<String?> = _displayName

    // LiveData holding the user's image URL.
    private val _userImage = MutableLiveData<String?>()
    val userImage: LiveData<String?> = _userImage

    // LiveData for error messages.
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        // If the user is already signed in, initialize the display name and image.
        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
            _displayName.value = firebaseUser.displayName
            _userImage.value = firebaseUser.photoUrl?.toString()
        }
    }

    // Sign in with email and password.
    fun signIn(email: String, password: String) {
        model.signIn(email, password) { firebaseUser, errorMessage ->
            if (firebaseUser != null) {
                _user.postValue(firebaseUser)
                _displayName.postValue(firebaseUser.displayName)
                _userImage.postValue(firebaseUser.photoUrl?.toString())
                // Optionally fetch additional user details from your model.
                fetchUserDetails(firebaseUser.uid)
            } else {
                _error.postValue(errorMessage ?: "Unknown error during sign in.")
            }
        }
    }

    // Sign up with email, password, and name.
    fun signUp(email: String, password: String, name: String) {
        model.signUp(email, password, name) { firebaseUser, errorMessage ->
            if (firebaseUser != null) {
                // Update the FirebaseUser profile with the display name.
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                firebaseUser.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // After profile update, update LiveData.
                        _user.postValue(firebaseUser)
                        _displayName.postValue(firebaseUser.displayName)
                        _userImage.postValue(firebaseUser.photoUrl?.toString())
                        // Optionally fetch additional details from your model.
                        fetchUserDetails(firebaseUser.uid)
                    } else {
                        _error.postValue("Failed to update user profile.")
                    }
                }
            } else {
                _error.postValue(errorMessage ?: "Unknown error during sign up.")
            }
        }
    }

    // Fetch additional user details from your model (if needed).
    private fun fetchUserDetails(userId: String) {
        model.getUser(userId) { userData ->
            if (userData != null) {
                _displayName.postValue(userData.name)
                _userImage.postValue(userData.image)
            } else {
                _error.postValue("Failed to load user details.")
            }
        }
    }

    // Sign out the current user.
    fun signOut() {
        model.signOut()
        _user.postValue(null)
        _displayName.postValue(null)
        _userImage.postValue(null)
    }
}