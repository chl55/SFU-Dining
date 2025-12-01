package cmpt362.group29.sfudining.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cmpt362.group29.sfudining.MainActivity
import cmpt362.group29.sfudining.ui.theme.OnSFURedContainer
import cmpt362.group29.sfudining.ui.theme.SFURed
import cmpt362.group29.sfudining.ui.theme.SFURedContainer

class AuthActivity : ComponentActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (authRepository.getCurrentUser() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            setContent {
                AuthScreen()
            }
        }
    }

    private fun onSignInClick(context: Context) {
        context.startActivity(Intent(context, SignInActivity::class.java))
    }

    private fun onSignUpClick(context: Context) {
        context.startActivity(Intent(context, SignUpActivity::class.java))
    }

    @Composable
    fun AuthScreen() {
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SFURed)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to SFU Dining!",
                style = MaterialTheme.typography.headlineLarge,
                color = SFURedContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = { onSignUpClick(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SFURedContainer)
            ) {
                Text("Sign Up", color = OnSFURedContainer)
            }

            Button(
                onClick = { onSignInClick(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SFURedContainer)
            ) {
                Text("Sign In", color = OnSFURedContainer)
            }
        }
    }
}
