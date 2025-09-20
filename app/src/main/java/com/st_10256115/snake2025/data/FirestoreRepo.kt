package com.st_10256115.snake2025.data


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class Score(
    val uid: String = "",
    val username: String = "",
    val score: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

class FirestoreRepo(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val scores = db.collection("scores")
    private val users  = db.collection("users")

    suspend fun saveUsernameOnce(username: String) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).set(mapOf("username" to username)).await()
    }

    suspend fun getUsername(uid: String): String {
        val doc = users.document(uid).get().await()
        return doc.getString("username") ?: "Player"
    }

    suspend fun submitScore(score: Int) {
        val uid = auth.currentUser?.uid ?: return
        val username = getUsername(uid)
        scores.add(Score(uid, username, score)).await()
    }

    suspend fun topScores(limit: Long = 10): List<Score> {
        val snap = scores.orderBy("score", Query.Direction.DESCENDING)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(limit).get().await()
        return snap.toObjects(Score::class.java)
    }
}