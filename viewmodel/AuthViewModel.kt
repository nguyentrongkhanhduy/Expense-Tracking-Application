package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    data class AuthUiState(
        val isSuccess: Boolean = false,
        val errorMessage: String? = null
    )

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState

    fun signIn(email: String, password: String) {
        _authState.value = AuthUiState()
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                _authState.value = AuthUiState(isSuccess = true)
            }
            .addOnFailureListener { ex ->
                _authState.value = AuthUiState(errorMessage = ex.localizedMessage)
            }
    }

    fun signUp(email: String, password: String) {
        _authState.value = AuthUiState()
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                _authState.value = AuthUiState(isSuccess = true)
            }
            .addOnFailureListener { ex ->
                _authState.value = AuthUiState(errorMessage = ex.localizedMessage)
            }
    }

    fun resetError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
}
