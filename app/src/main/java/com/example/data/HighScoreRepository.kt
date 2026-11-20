package com.example.data

import com.example.model.DifficultyLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HighScoreRepository(
    private val dao: HighScoreDao,
    private val prefs: PreferencesRepository
) {
    val allScores: Flow<List<HighScoreEntity>> = dao.observeAllScores()

    val overallBestScore: Flow<Int> = dao.observeOverallTopScore().map { it ?: 0 }

    val bestScoresByDifficulty: Flow<Map<DifficultyLevel, Int>> = combine(
        dao.observeTopScoreForDifficulty(DifficultyLevel.EASY.name),
        dao.observeTopScoreForDifficulty(DifficultyLevel.NORMAL.name),
        dao.observeTopScoreForDifficulty(DifficultyLevel.HARD.name)
    ) { easy, normal, hard ->
        mapOf(
            DifficultyLevel.EASY to (easy ?: 0),
            DifficultyLevel.NORMAL to (normal ?: 0),
            DifficultyLevel.HARD to (hard ?: 0)
        )
    }

    val soundEnabled: Flow<Boolean> = prefs.soundEnabled
    val lastPlayerName: Flow<String> = prefs.lastPlayerName

    suspend fun saveScore(
        playerName: String,
        score: Int,
        difficulty: DifficultyLevel,
        bestCombo: Int,
        foodsFed: Int
    ) {
        val cleanName = playerName.trim().ifEmpty { "Majikan MOCHI" }.take(24)
        prefs.setLastPlayerName(cleanName)
        dao.insertScore(
            HighScoreEntity(
                playerName = cleanName,
                score = score,
                difficultyName = difficulty.name,
                bestCombo = bestCombo,
                foodsFed = foodsFed
            )
        )
    }

    suspend fun clearAllScores() {
        dao.clearAll()
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        prefs.setSoundEnabled(enabled)
    }
}
