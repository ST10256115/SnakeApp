package com.st_10256115.snake2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.st_10256115.snake2025.auth.AuthScreen
import com.st_10256115.snake2025.data.FirestoreRepo
import com.st_10256115.snake2025.game.GameScreen
import com.st_10256115.snake2025.leaderboard.LeaderboardScreen
import com.st_10256115.snake2025.nav.Screen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            MaterialTheme {
                val repo = remember { FirestoreRepo() }
                val nav = rememberNavController()
                val auth = remember { FirebaseAuth.getInstance() }

                Scaffold { paddingValues ->
                    NavHost(
                        navController = nav,
                        startDestination = if (auth.currentUser == null) Screen.Auth.route else Screen.Game.route,
                        modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.Auth.route) {
                            AuthScreen(
                                onAuthed = {
                                    nav.navigate(Screen.Game.route) {
                                        popUpTo(Screen.Auth.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Game.route) {
                            GameScreen(
                                repo = repo,
                                onGameOverDone = { nav.navigate(Screen.Leaderboard.route) },
                                ensureUsername = {
                                    val uid = auth.currentUser?.uid
                                    if (uid != null) {
                                        val name = repo.getUsername(uid) // suspend OK
                                        if (name == "Player") {
                                            nav.navigate(Screen.Username.route)
                                        }
                                    }
                                }
                            )
                        }
                        composable(Screen.Leaderboard.route) {
                            LeaderboardScreen(
                                repo = repo,
                                onBack = { nav.popBackStack() }
                            )
                        }
                        composable(Screen.Username.route) {
                            val scope = rememberCoroutineScope()
                            UsernamePrompt(
                                onSave = { username ->
                                    scope.launch {
                                        repo.saveUsernameOnce(username)
                                        nav.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsernamePrompt(onSave: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Set Username") },
        text = {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (username.isNotBlank()) onSave(username.trim()) }) {
                Text("Save")
            }
        }
    )
}
