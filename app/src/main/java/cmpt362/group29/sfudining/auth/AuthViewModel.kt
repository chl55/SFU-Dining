package cmpt362.group29.sfudining.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun signUp(onResult: (Boolean, String?) -> Unit) {
        AuthRepository().signUp(email, password, onResult)
    }

    fun signIn(onResult: (Boolean, String?) -> Unit) {
        AuthRepository().signIn(email, password, onResult)
    }

    fun signOut() {
        AuthRepository().signOut()
    }

    fun getUserEmail(): String? {
        return AuthRepository().getUserEmail()
    }
}