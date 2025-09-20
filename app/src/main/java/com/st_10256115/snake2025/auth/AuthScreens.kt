// auth/AuthScreens.kt
package com.st_10256115.snake2025.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onAuthed: () -> Unit,
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }     // kept for future use
    var mode by remember { mutableStateOf(AuthMode.Login) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(Modifier.padding(24.dp)) {
        Text(
            if (mode == AuthMode.Login) "Login" else "Register",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))

        if (mode == AuthMode.Register) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                enabled = !loading,
                onClick = {
                    loading = true
                    error = null
                    scope.launch {
                        try {
                            if (mode == AuthMode.Login) {
                                auth.signInWithEmailAndPassword(email.trim(), password).await()
                            } else {
                                auth.createUserWithEmailAndPassword(email.trim(), password).await()
                                // optional: store `username` after register in Firestore later
                            }
                            onAuthed()
                        } catch (e: Exception) {
                            error = e.message ?: "Auth failed"
                        } finally {
                            loading = false
                        }
                    }
                }
            ) {
                Text(if (mode == AuthMode.Login) "Login" else "Register")
            }

            OutlinedButton(onClick = {
                mode = if (mode == AuthMode.Login) AuthMode.Register else AuthMode.Login
            }) {
                Text(if (mode == AuthMode.Login) "Need an account?" else "Have an account?")
            }
        }
    }
}

enum class AuthMode { Login, Register }
