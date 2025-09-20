package com.st_10256115.snake2025.nav

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Game : Screen("game")
    object Leaderboard : Screen("leaderboard")
    object Username : Screen("username")
}