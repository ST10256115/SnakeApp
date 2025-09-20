package com.st_10256115.snake2025.game


import kotlin.random.Random

enum class Direction { Up, Down, Left, Right }

data class Point(val x: Int, val y: Int)

data class GameState(
    val width: Int,
    val height: Int,
    val snake: List<Point>,
    val dir: Direction,
    val food: Point,
    val score: Int = 0,
    val alive: Boolean = true
)

fun initialState(w: Int, h: Int): GameState {
    val start = Point(w / 2, h / 2)
    val food = randomFreeCell(listOf(start), w, h)
    return GameState(w, h, snake = listOf(start), dir = Direction.Right, food = food)
}

private fun randomFreeCell(occupied: List<Point>, w: Int, h: Int): Point {
    while (true) {
        val p = Point(Random.nextInt(w), Random.nextInt(h))
        if (p !in occupied) return p
    }
}

fun step(state: GameState): GameState {
    if (!state.alive) return state
    val head = state.snake.first()
    val next = when (state.dir) {
        Direction.Up -> Point(head.x, head.y - 1)
        Direction.Down -> Point(head.x, head.y + 1)
        Direction.Left -> Point(head.x - 1, head.y)
        Direction.Right -> Point(head.x + 1, head.y)
    }

    // collisions
    if (next.x !in 0 until state.width || next.y !in 0 until state.height) {
        return state.copy(alive = false)
    }
    if (next in state.snake) {
        return state.copy(alive = false)
    }

    val ate = next == state.food
    val newSnake = buildList {
        add(next)
        addAll(state.snake.take(if (ate) state.snake.size else state.snake.size - 1))
    }
    val newFood = if (ate) randomFreeCell(newSnake, state.width, state.height) else state.food
    val newScore = if (ate) state.score + 10 else state.score
    return state.copy(snake = newSnake, food = newFood, score = newScore)
}

fun turn(current: Direction, intended: Direction): Direction {
    // Disallow 180° flips
    return when {
        current == Direction.Up && intended == Direction.Down -> current
        current == Direction.Down && intended == Direction.Up -> current
        current == Direction.Left && intended == Direction.Right -> current
        current == Direction.Right && intended == Direction.Left -> current
        else -> intended
    }
}