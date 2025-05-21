package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.User
import com.example.myapplication.services.RetrofitClient
import com.example.myapplication.services.SignUpRequest
import com.example.myapplication.services.TokenRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow<String?>(null)
    val authState: StateFlow<String?> = _authState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    result.user?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
                        val idToken = tokenResult.token ?: return@addOnSuccessListener

                        viewModelScope.launch {
                            try {
                                val response = RetrofitClient.instance.signIn(TokenRequest(idToken))
                                _authState.value = "Sign in successful: ${response.displayName}"
                                _user.value = response
                                println("User: ${response.email}")
                            } catch (e: Exception) {
                                _authState.value = "Sign in failed: ${e.message}"
                                println("Error: ${e.message}")
                            }
                        }
                    }

                }
                .addOnFailureListener { exception ->
                    _authState.value = "Sign in failed: ${exception.message}"
                    println("Error: ${exception.message}")
                }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        val request = SignUpRequest(email, password, displayName)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.signUp(request)
                _authState.value = "Sign up successful: ${response.displayName}"
                _user.value = response
                println("User: ${response.email}")
            } catch (e: Exception) {
                _authState.value = "Sign up failed: ${e.message}"
                println("Error: ${e.message}")
            }
        }
    }
}