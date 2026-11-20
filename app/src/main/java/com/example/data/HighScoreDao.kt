package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC, bestCombo DESC, createdAt DESC LIMIT 50")
    fun observeAllScores(): Flow<List<HighScoreEntity>>

    @Query("SELECT MAX(score) FROM high_scores WHERE difficultyName = :difficultyName")
    fun observeTopScoreForDifficulty(difficultyName: String): Flow<Int?>

    @Query("SELECT MAX(score) FROM high_scores")
    fun observeOverallTopScore(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(entry: HighScoreEntity): Long

    @Query("DELETE FROM high_scores")
    suspend fun clearAll()
}
