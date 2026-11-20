package com.example.model

import androidx.compose.ui.graphics.Color

enum class DifficultyLevel(
    val displayName: String,
    val subtitle: String,
    val maxLives: Int,
    val initialPatienceMs: Long,
    val minPatienceMs: Long,
    val patienceStepDownMs: Long,
    val basePoints: Int,
    val traySize: Int,
    val trapCount: Int,
    val shuffleIntervalRounds: Int,
    val accentColor: Color
) {
    EASY(
        displayName = "Santai",
        subtitle = "5 nyawa • Waktu 6.5 dtk • Tanpa jebakan",
        maxLives = 5,
        initialPatienceMs = 6500L,
        minPatienceMs = 3800L,
        patienceStepDownMs = 90L,
        basePoints = 10,
        traySize = 4,
        trapCount = 0,
        shuffleIntervalRounds = 0,
        accentColor = Color(0xFF2B8A67)
    ),
    NORMAL(
        displayName = "Seru",
        subtitle = "3 nyawa • Waktu 4.2 dtk • Ada jebakan",
        maxLives = 3,
        initialPatienceMs = 4200L,
        minPatienceMs = 2300L,
        patienceStepDownMs = 110L,
        basePoints = 20,
        traySize = 6,
        trapCount = 1,
        shuffleIntervalRounds = 3,
        accentColor = Color(0xFFE5631F)
    ),
    HARD(
        displayName = "Ganas",
        subtitle = "3 nyawa • Waktu 2.8 dtk • Acak cepat!",
        maxLives = 3,
        initialPatienceMs = 2800L,
        minPatienceMs = 1550L,
        patienceStepDownMs = 120L,
        basePoints = 35,
        traySize = 6,
        trapCount = 2,
        shuffleIntervalRounds = 1,
        accentColor = Color(0xFFD94366)
    )
}

enum class CatMood {
    HUNGRY,
    HAPPY,
    ANGRY,
    GAME_OVER
}

enum class FoodType(
    val displayName: String,
    val shortHint: String,
    val isTrap: Boolean = false
) {
    SALMON("Ikan Salmon", "Segar & gurih"),
    MILK("Susu Hangat", "Manis creamy"),
    SHRIMP("Udang Rebus", "Kenyal favorit"),
    CHICKEN("Ayam Panggang", "Harum sedap"),
    TUNA("Mangkuk Tuna", "Porsi mantap"),
    BISCUIT("Biskuit Ikan", "Renyah kriuk"),
    CHILI("Cabai Pedas", "Jebakan! Jangan kasih!", isTrap = true),
    FISHBONE("Duri Ikan", "Jebakan! Bikin keselek!", isTrap = true);

    companion object {
        val treats: List<FoodType> = entries.filter { !it.isTrap }
        val traps: List<FoodType> = entries.filter { it.isTrap }
    }
}
