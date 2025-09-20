package com.st_10256115.snake2025.leaderboard


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.st_10256115.snake2025.data.FirestoreRepo
import com.st_10256115.snake2025.data.Score
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(
    repo: FirestoreRepo,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var scores by remember { mutableStateOf<List<Score>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            scores = repo.topScores(10)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Top 10", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(scores) { idx, s ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${idx + 1}. ${s.username}")
                        Text("${s.score}")
                    }
                }
            }
        }
    }
}