package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
import com.example.myapplication.services.AuthApiService
import com.example.myapplication.services.FcmTokenRequest
import com.example.myapplication.services.RetrofitClient
import com.example.myapplication.services.SignUpRequest
import com.example.myapplication.services.TokenRequest
import com.example.myapplication.services.UserMessagePreference
import com.github.mikephil.charting.utils.Utils.init
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    private val _shouldPromtSync = MutableStateFlow(false)
    val shouldPromtSync: StateFlow<Boolean> = _shouldPromtSync

    // NEW: Error message state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun setSyncPrompt(shouldPrompt: Boolean) {
        _shouldPromtSync.value = shouldPrompt
    }

    private val authService =

        /*---- For Android Studio  ----*/
//        RetrofitClient.createService(AuthApiService::class.java, "http://10.0.2.2:3000")

    /*---- For Physical Device  ----*/
        RetrofitClient.createService(AuthApiService::class.java, "https://expense-app-server-mocha.vercel.app")

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                try {
                    val response = authService.signIn(
                        TokenRequest(
                            firebaseUser.getIdToken(true).await().token!!
                        )
                    )
                    _user.value = response
                    _isSignedIn.value = true
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
        }
    }

    companion object {
        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }


        fun isValidPassword(password: String): Boolean {
            val passwordRegex = Regex(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{6,}$"
            )
            return passwordRegex.matches(password)
        }
    }


    fun signIn(email: String, password: String, onSignInSuccess: (userId: String) -> Unit) {
        _isLoading.value = true
        _isSignedIn.value = false
        _errorMessage.value = null
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
                                onSignInSuccess(response.uid)
                                println("User: ${response.email}")
                            } catch (e: Exception) {
                                println("Error: ${e.message}")
                                _isLoading.value = false
                                _errorMessage.value = "Login failed. Please try again."
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error: ${exception.message}")
                    _isLoading.value = false
                    _errorMessage.value = "Incorrect email or password"
                }
        }
    }

    fun signUp(
        email: String,
        password: String,
        displayName: String,
        onSignUpSuccess: (userId: String) -> Unit
    ) {
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
                auth.signInWithEmailAndPassword(email, password)
                onSignUpSuccess(response.uid)
                println("User: ${response.email}")
            } catch (e: Exception) {
                println("Error: ${e.message}")
                _isLoading.value = false
                _errorMessage.value = "Sign up failed. Please try again."
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
        _isSignedIn.value = false
        _isSignedUp.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun sendFCMTokenToServer(userId: String, token: String) {
        viewModelScope.launch {
            try {
                val response = authService.updateFcmToken(FcmTokenRequest(userId, token))
                println("Response: $response")
                println("Token sent: userId: $userId, token: $token")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    fun setMessagePreference(userId: String, messagePreference: String) {
        viewModelScope.launch {
            try {
                val response = authService.updateMessagePreference(
                    UserMessagePreference(
                        userId,
                        messagePreference
                    )
                )
                println("Response: $response")
                println("Message preference sent: userId: $userId, messagePreference: $messagePreference")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }
}
