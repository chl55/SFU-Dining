package cmpt362.group29.sfudining.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cmpt362.group29.sfudining.MainActivity
import cmpt362.group29.sfudining.ui.theme.OnSFURedContainer
import cmpt362.group29.sfudining.ui.theme.SFURed
import cmpt362.group29.sfudining.ui.theme.SFURedContainer

class SignInActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignInScreen(
                authViewModel = authViewModel,
                onBackClick = { finish() },
                onSubmitClick = {
                    authViewModel.signIn { success, errorMsg ->
                        if (success) {
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, errorMsg ?: "Sign in failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }

    @Composable
    fun SignInScreen(
        authViewModel: AuthViewModel,
        onSubmitClick: () -> Unit,
        onBackClick: () -> Unit
    ) {
        val email = authViewModel.email
        val password = authViewModel.password

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SFURed)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineLarge,
                color = SFURedContainer,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = authViewModel::onEmailChange,
                label = { Text("Email", color = OnSFURedContainer) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSFURedContainer,
                    unfocusedTextColor = OnSFURedContainer,
                    disabledTextColor = OnSFURedContainer,
                    errorTextColor = OnSFURedContainer,
                    focusedContainerColor = SFURedContainer,
                    unfocusedContainerColor = SFURedContainer,
                    disabledContainerColor = SFURedContainer,
                    errorContainerColor = SFURedContainer,
                    cursorColor = SFURed,
                    errorCursorColor = SFURed,
                    selectionColors = null,
                    focusedBorderColor = OnSFURedContainer,
                    unfocusedBorderColor = OnSFURedContainer,
                    disabledBorderColor = OnSFURedContainer,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedLabelColor = SFURedContainer,
                    unfocusedLabelColor = SFURedContainer,
                    disabledLabelColor = OnSFURedContainer,
                    errorLabelColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = authViewModel::onPasswordChange,
                label = { Text("Password", color = OnSFURedContainer) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSFURedContainer,
                    unfocusedTextColor = OnSFURedContainer,
                    disabledTextColor = OnSFURedContainer,
                    errorTextColor = OnSFURedContainer,
                    focusedContainerColor = SFURedContainer,
                    unfocusedContainerColor = SFURedContainer,
                    disabledContainerColor = SFURedContainer,
                    errorContainerColor = SFURedContainer,
                    cursorColor = SFURed,
                    errorCursorColor = SFURed,
                    selectionColors = null,
                    focusedBorderColor = OnSFURedContainer,
                    unfocusedBorderColor = OnSFURedContainer,
                    disabledBorderColor = OnSFURedContainer,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedLabelColor = SFURedContainer,
                    unfocusedLabelColor = SFURedContainer,
                    disabledLabelColor = OnSFURedContainer,
                    errorLabelColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmitClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SFURedContainer)
            ) {
                Text("Sign In", color = OnSFURedContainer)
            }

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SFURedContainer)
            ) {
                Text("Back", color = OnSFURedContainer)
            }
        }
    }
}
