package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffects
import com.example.data.AppDatabase
import com.example.data.HighScoreEntity
import com.example.data.HighScoreRepository
import com.example.data.PreferencesRepository
import com.example.model.CatMood
import com.example.model.DifficultyLevel
import com.example.model.FoodType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

enum class AppScreen {
    HOME,
    GAME,
    HIGH_SCORES
}

data class FeedbackPopup(
    val id: Long,
    val message: String,
    val isPositive: Boolean
)

data class FedFoodEvent(
    val id: Long,
    val food: FoodType,
    val isPositive: Boolean
)

data class GameUiState(
    val difficulty: DifficultyLevel = DifficultyLevel.NORMAL,
    val score: Int = 0,
    val lives: Int = DifficultyLevel.NORMAL.maxLives,
    val combo: Int = 0,
    val bestCombo: Int = 0,
    val foodsFed: Int = 0,
    val round: Int = 1,
    val mood: CatMood = CatMood.HUNGRY,
    val speechText: String = "Meong~ MOCHI lapar nih!",
    val targetFood: FoodType = FoodType.SALMON,
    val trayItems: List<FoodType> = FoodType.treats.take(6),
    val patienceMaxMs: Long = DifficultyLevel.NORMAL.initialPatienceMs,
    val patienceRemainingMs: Long = DifficultyLevel.NORMAL.initialPatienceMs,
    val isGameOver: Boolean = false,
    val isNewRecord: Boolean = false,
    val scoreSaved: Boolean = false,
    val feedbackPopup: FeedbackPopup? = null,
    val lastFedEvent: FedFoodEvent? = null
) {
    val patienceProgress: Float
        get() = if (patienceMaxMs <= 0L) 0f else (patienceRemainingMs.toFloat() / patienceMaxMs.toFloat()).coerceIn(0f, 1f)
}

class MeongViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HighScoreRepository
    private val soundEffects = SoundEffects(application)

    init {
        val db = AppDatabase.getInstance(application)
        val prefs = PreferencesRepository(application)
        repository = HighScoreRepository(db.highScoreDao(), prefs)
    }

    val allScores: StateFlow<List<HighScoreEntity>> = repository.allScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallBestScore: StateFlow<Int> = repository.overallBestScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bestScoresByDifficulty: StateFlow<Map<DifficultyLevel, Int>> = repository.bestScoresByDifficulty
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DifficultyLevel.entries.associateWith { 0 }
        )

    val soundEnabled: StateFlow<Boolean> = repository.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val lastPlayerName: StateFlow<String> = repository.lastPlayerName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Majikan MOCHI")

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow(DifficultyLevel.NORMAL)
    val selectedDifficulty: StateFlow<DifficultyLevel> = _selectedDifficulty.asStateFlow()

    private val _homePreviewMood = MutableStateFlow(CatMood.HUNGRY)
    val homePreviewMood: StateFlow<CatMood> = _homePreviewMood.asStateFlow()

    private val _gameState = MutableStateFlow(GameUiState())
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()

    private var timerJob: Job? = null
    private var moodResetJob: Job? = null
    private var popupCounter = 0L

    private val happyQuotes = listOf(
        "Nyam nyam! MOCHI suka banget!",
        "Purr~ MOCHI makin sayang Majikan!",
        "Mantap! MOCHI mau lagi dong!",
        "MOCHI seneng! Enak banget!"
    )

    private val angryWrongQuotes = listOf(
        "HISSS!! MOCHI nggak mau itu!",
        "MOCHI kesel! Salah suap!",
        "Grrr! Jangan ngasal kasih MOCHI!"
    )

    private val angryTimeoutQuotes = listOf(
        "LAMA BANGET!! MOCHI kelaperan!",
        "MOCHI ngambek! Keburu dingin!",
        "Hmph! MOCHI kok nggak disuapin?!"
    )

    fun selectDifficulty(difficulty: DifficultyLevel) {
        _selectedDifficulty.value = difficulty
    }

    fun cycleHomePreviewMood() {
        val next = when (_homePreviewMood.value) {
            CatMood.HUNGRY -> CatMood.HAPPY
            CatMood.HAPPY -> CatMood.ANGRY
            CatMood.ANGRY -> CatMood.GAME_OVER
            CatMood.GAME_OVER -> CatMood.HUNGRY
        }
        _homePreviewMood.value = next
        when (next) {
            CatMood.HAPPY -> soundEffects.playHappyChime(soundEnabled.value)
            CatMood.ANGRY -> soundEffects.playAngryBuzz(soundEnabled.value)
            CatMood.HUNGRY -> soundEffects.playHungryMeow(soundEnabled.value)
            CatMood.GAME_OVER -> soundEffects.playGameOver(soundEnabled.value)
        }
    }

    fun toggleSound() {
        val next = !soundEnabled.value
        viewModelScope.launch {
            repository.setSoundEnabled(next)
        }
    }

    fun openHighScores() {
        stopGameLoop()
        _currentScreen.value = AppScreen.HIGH_SCORES
    }

    fun navigateHome() {
        stopGameLoop()
        _currentScreen.value = AppScreen.HOME
    }

    fun startGame(difficulty: DifficultyLevel = _selectedDifficulty.value) {
        _selectedDifficulty.value = difficulty
        stopGameLoop()

        val initialTarget = FoodType.treats.random()
        val initialTray = buildTray(difficulty = difficulty, target = initialTarget, currentTray = null, forceShuffle = true)
        val initialPatience = difficulty.initialPatienceMs

        _gameState.value = GameUiState(
            difficulty = difficulty,
            score = 0,
            lives = difficulty.maxLives,
            combo = 0,
            bestCombo = 0,
            foodsFed = 0,
            round = 1,
            mood = CatMood.HUNGRY,
            speechText = "Meong~ MOCHI mau ${initialTarget.displayName}!",
            targetFood = initialTarget,
            trayItems = initialTray,
            patienceMaxMs = initialPatience,
            patienceRemainingMs = initialPatience,
            isGameOver = false,
            isNewRecord = false,
            scoreSaved = false,
            feedbackPopup = null,
            lastFedEvent = null
        )

        soundEffects.playHungryMeow(soundEnabled.value)
        _currentScreen.value = AppScreen.GAME
        startTimerLoop()
    }

    fun feedCat(food: FoodType) {
        val state = _gameState.value
        if (state.isGameOver) return

        val difficulty = state.difficulty
        if (food == state.targetFood && !food.isTrap) {
            val newCombo = state.combo + 1
            val newBestCombo = max(state.bestCombo, newCombo)
            val multiplier = (1 + (newCombo - 1) / 3).coerceAtMost(4)
            val gained = difficulty.basePoints * multiplier
            val newScore = state.score + gained
            val newFoodsFed = state.foodsFed + 1
            val nextRound = state.round + 1

            val nextTarget = pickNextTarget(previous = state.targetFood)
            val shouldShuffle = difficulty.shuffleIntervalRounds > 0 && (nextRound % difficulty.shuffleIntervalRounds == 0)
            val nextTray = buildTray(
                difficulty = difficulty,
                target = nextTarget,
                currentTray = state.trayItems,
                forceShuffle = shouldShuffle
            )
            val nextPatience = calculatePatienceMs(difficulty, nextRound)

            soundEffects.playHappyChime(soundEnabled.value)
            popupCounter++
            val popupText = if (newCombo >= 2) "+$gained • KOMBO x$newCombo!" else "+$gained"

            _gameState.update {
                it.copy(
                    score = newScore,
                    combo = newCombo,
                    bestCombo = newBestCombo,
                    foodsFed = newFoodsFed,
                    round = nextRound,
                    mood = CatMood.HAPPY,
                    speechText = "${happyQuotes.random()} Sekarang mau ${nextTarget.displayName}!",
                    targetFood = nextTarget,
                    trayItems = nextTray,
                    patienceMaxMs = nextPatience,
                    patienceRemainingMs = nextPatience,
                    feedbackPopup = FeedbackPopup(popupCounter, popupText, isPositive = true),
                    lastFedEvent = FedFoodEvent(popupCounter, food, isPositive = true)
                )
            }
            scheduleMoodReset()
        } else {
            val reason = if (food.isTrap) {
                "HISSS!! ${food.displayName} bikin MOCHI marah!!"
            } else {
                angryWrongQuotes.random()
            }
            handleMistake(
                speech = reason,
                popupMessage = if (food.isTrap) "Jebakan! -1 ❤️" else "Salah! -1 ❤️",
                fedFood = food
            )
        }
    }

    private fun handleMistake(speech: String, popupMessage: String, fedFood: FoodType? = null) {
        val state = _gameState.value
        if (state.isGameOver) return

        val remainingLives = state.lives - 1
        val difficulty = state.difficulty
        popupCounter++

        val fedEvent = fedFood?.let { FedFoodEvent(popupCounter, it, isPositive = false) }

        if (remainingLives <= 0) {
            stopGameLoop()
            soundEffects.playGameOver(soundEnabled.value)
            val previousBest = bestScoresByDifficulty.value[difficulty] ?: 0
            val isNewBest = state.score > 0 && state.score > previousBest

            _gameState.update {
                it.copy(
                    lives = 0,
                    combo = 0,
                    mood = CatMood.GAME_OVER,
                    speechText = "Hiks... MOCHI sedih & ngambek! Game Over!",
                    patienceRemainingMs = 0L,
                    isGameOver = true,
                    isNewRecord = isNewBest,
                    feedbackPopup = FeedbackPopup(popupCounter, popupMessage, isPositive = false),
                    lastFedEvent = fedEvent
                )
            }
        } else {
            soundEffects.playAngryBuzz(soundEnabled.value)
            val nextRound = state.round + 1
            val nextTarget = pickNextTarget(previous = state.targetFood)
            val nextTray = buildTray(
                difficulty = difficulty,
                target = nextTarget,
                currentTray = state.trayItems,
                forceShuffle = difficulty == DifficultyLevel.HARD
            )
            val nextPatience = calculatePatienceMs(difficulty, nextRound)

            _gameState.update {
                it.copy(
                    lives = remainingLives,
                    combo = 0,
                    round = nextRound,
                    mood = CatMood.ANGRY,
                    speechText = "$speech (Mau ${nextTarget.displayName}!)",
                    targetFood = nextTarget,
                    trayItems = nextTray,
                    patienceMaxMs = nextPatience,
                    patienceRemainingMs = nextPatience,
                    feedbackPopup = FeedbackPopup(popupCounter, popupMessage, isPositive = false),
                    lastFedEvent = fedEvent
                )
            }
            scheduleMoodReset()
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val tickMs = 50L
            while (true) {
                delay(tickMs)
                val state = _gameState.value
                if (state.isGameOver) break
                val nextRemaining = state.patienceRemainingMs - tickMs
                if (nextRemaining <= 0L) {
                    handleMistake(
                        speech = angryTimeoutQuotes.random(),
                        popupMessage = "Telat! -1 ❤️"
                    )
                } else {
                    _gameState.update { it.copy(patienceRemainingMs = nextRemaining) }
                }
            }
        }
    }

    private fun scheduleMoodReset() {
        moodResetJob?.cancel()
        moodResetJob = viewModelScope.launch {
            delay(900L)
            _gameState.update { state ->
                if (!state.isGameOver && state.mood != CatMood.HUNGRY) {
                    state.copy(
                        mood = CatMood.HUNGRY,
                        speechText = "Meong~ MOCHI mau ${state.targetFood.displayName}!"
                    )
                } else {
                    state
                }
            }
        }
    }

    private fun stopGameLoop() {
        timerJob?.cancel()
        timerJob = null
        moodResetJob?.cancel()
        moodResetJob = null
    }

    fun saveCurrentScore(playerName: String) {
        val state = _gameState.value
        if (state.scoreSaved || state.score <= 0) {
            _gameState.update { it.copy(scoreSaved = true) }
            return
        }
        viewModelScope.launch {
            repository.saveScore(
                playerName = playerName,
                score = state.score,
                difficulty = state.difficulty,
                bestCombo = state.bestCombo,
                foodsFed = state.foodsFed
            )
            _gameState.update { it.copy(scoreSaved = true) }
        }
    }

    fun clearAllScores() {
        viewModelScope.launch {
            repository.clearAllScores()
        }
    }

    private fun pickNextTarget(previous: FoodType): FoodType {
        val candidates = FoodType.treats.filter { it != previous }
        return candidates.randomOrNull() ?: FoodType.SALMON
    }

    private fun calculatePatienceMs(difficulty: DifficultyLevel, round: Int): Long {
        val reduction = (round - 1) * difficulty.patienceStepDownMs
        return (difficulty.initialPatienceMs - reduction).coerceAtLeast(difficulty.minPatienceMs)
    }

    private fun buildTray(
        difficulty: DifficultyLevel,
        target: FoodType,
        currentTray: List<FoodType>?,
        forceShuffle: Boolean
    ): List<FoodType> {
        if (!forceShuffle && currentTray != null && currentTray.contains(target) && currentTray.size == difficulty.traySize) {
            return currentTray
        }
        val trapsNeeded = difficulty.trapCount.coerceAtMost(FoodType.traps.size)
        val selectedTraps = FoodType.traps.shuffled(Random).take(trapsNeeded)
        val treatsNeeded = (difficulty.traySize - trapsNeeded - 1).coerceAtLeast(0)
        val otherTreats = FoodType.treats
            .filter { it != target }
            .shuffled(Random)
            .take(treatsNeeded)

        return (listOf(target) + otherTreats + selectedTraps).shuffled(Random)
    }

    override fun onCleared() {
        stopGameLoop()
        soundEffects.release()
        super.onCleared()
    }
}
