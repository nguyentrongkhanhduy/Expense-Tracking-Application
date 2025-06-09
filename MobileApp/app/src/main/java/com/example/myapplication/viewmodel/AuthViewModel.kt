package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
import com.example.myapplication.services.AuthApiService
import com.example.myapplication.services.RetrofitClient
import com.example.myapplication.services.SignUpRequest
import com.example.myapplication.services.TokenRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isSignedUp = MutableStateFlow(false)
    val isSignedUp: StateFlow<Boolean> = _isSignedUp

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val authService = RetrofitClient.createService(AuthApiService::class.java, "http://10.0.2.2:3000")

    fun signIn(email: String, password: String) {
        _isLoading.value = true
        _isSignedIn.value = false
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    result.user?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
                        val idToken = tokenResult.token ?: return@addOnSuccessListener
                        viewModelScope.launch {
                            try {
                                val response = authService.signIn(TokenRequest(idToken))
                                _isSignedIn.value = true
                                _isLoading.value = false
                                _user.value = response
                                println("User: ${response.email}")
                            } catch (e: Exception) {
                                println("Error: ${e.message}")
                                _isLoading.value = false
                            }
                        }
                    }

                }
                .addOnFailureListener { exception ->
                    println("Error: ${exception.message}")
                    _isLoading.value = false
                }
        }
    }

    fun signUp(email: String, password: String, displayName: String, onSignUpSuccess: (userId: String) -> Unit) {
        _isSignedUp.value = false
        _isLoading.value = true
        val request = SignUpRequest(email, password, displayName)
        viewModelScope.launch {
            try {
                val response = authService.signUp(request)
                _user.value = response
                _isSignedUp.value = true
                _isLoading.value = false
                _isSignedIn.value = true
                onSignUpSuccess(response.uid)
                println("User: ${response.email}")
            } catch (e: Exception) {
                println("Error: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
        _isSignedIn.value = false
        _isSignedUp.value = false
    }
}