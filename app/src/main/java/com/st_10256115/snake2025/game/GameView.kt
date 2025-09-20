package com.st_10256115.snake2025.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.st_10256115.snake2025.data.FirestoreRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun GameScreen(
    repo: FirestoreRepo,
    onGameOverDone: () -> Unit,
    ensureUsername: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()
    var gridW by remember { mutableStateOf(20) }
    var gridH by remember { mutableStateOf(28) }
    var state by remember { mutableStateOf(initialState(gridW, gridH)) }
    var showOver by remember { mutableStateOf(false) }
    var tickMs by remember { mutableStateOf(120L) }

    // capture theme colors in composable scope (not inside Canvas draw block)
    val snakeColor = MaterialTheme.colorScheme.primary
    val foodColor = MaterialTheme.colorScheme.secondary
    val bgColor = MaterialTheme.colorScheme.surface

    // Game loop
    LaunchedEffect(state.alive, tickMs) {
        if (state.alive) {
            while (state.alive) {
                delay(tickMs)
                state = step(state)
            }
            showOver = true
        }
    }

    Column(Modifier.fillMaxSize().background(bgColor)) {
        // Score bar
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Score: ${state.score}", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = {
                state = initialState(gridW, gridH)
                showOver = false
            }) { Text("Restart") }
        }

        // Canvas + swipe
        Box(
            Modifier
                .weight(1f)
                .padding(12.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val (dx, dy) = dragAmount
                        if (abs(dx) > 18 || abs(dy) > 18) {
                            state = state.copy(
                                dir = turn(
                                    state.dir,
                                    if (abs(dx) > abs(dy)) {
                                        if (dx > 0) Direction.Right else Direction.Left
                                    } else {
                                        if (dy > 0) Direction.Down else Direction.Up
                                    }
                                )
                            )
                        }
                        change.consume()
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellW = size.width / gridW
                val cellH = size.height / gridH

                // food
                drawRect(
                    color = foodColor,
                    topLeft = Offset(state.food.x * cellW, state.food.y * cellH),
                    size = Size(cellW, cellH)
                )

                // snake
                state.snake.forEach { p ->
                    drawRect(
                        color = snakeColor,
                        topLeft = Offset(p.x * cellW, p.y * cellH),
                        size = Size(cellW, cellH)
                    )
                }
            }
        }

        // Buttons
        Column(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OutlinedButton(onClick = {
                    state = state.copy(dir = turn(state.dir, Direction.Up))
                }) { Text("↑") }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = {
                    state = state.copy(dir = turn(state.dir, Direction.Left))
                }) { Text("←") }
                OutlinedButton(onClick = {
                    state = state.copy(dir = turn(state.dir, Direction.Down))
                }) { Text("↓") }
                OutlinedButton(onClick = {
                    state = state.copy(dir = turn(state.dir, Direction.Right))
                }) { Text("→") }
            }
        }
    }

    if (showOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over") },
            text = { Text("Your score: ${state.score}") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        ensureUsername()
                        repo.submitScore(state.score)
                        onGameOverDone()
                    }
                }) { Text("Save & Leaderboard") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showOver = false
                    state = initialState(gridW, gridH)
                }) { Text("Play Again") }
            }
        )
    }
}
