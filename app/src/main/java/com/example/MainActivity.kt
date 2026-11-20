package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.BackgroundMusicPlayer
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HighScoresScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MeongLaparTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MeongViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeongLaparTheme {
                MeongLaparApp()
            }
        }
    }
}

@Composable
fun MeongLaparApp(
    viewModel: MeongViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsStateWithLifecycle()
    val homePreviewMood by viewModel.homePreviewMood.collectAsStateWithLifecycle()
    val overallBestScore by viewModel.overallBestScore.collectAsStateWithLifecycle()
    val bestScoresByDifficulty by viewModel.bestScoresByDifficulty.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val lastPlayerName by viewModel.lastPlayerName.collectAsStateWithLifecycle()
    val allScores by viewModel.allScores.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    val bgmPlayer = remember { BackgroundMusicPlayer() }
    var isForeground by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isForeground = true
                Lifecycle.Event.ON_PAUSE -> isForeground = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            bgmPlayer.release()
        }
    }

    LaunchedEffect(soundEnabled, isForeground) {
        bgmPlayer.updateState(soundEnabled = soundEnabled, appForeground = isForeground)
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.HOME -> {
                HomeScreen(
                    selectedDifficulty = selectedDifficulty,
                    previewMood = homePreviewMood,
                    overallBestScore = overallBestScore,
                    bestScoresByDifficulty = bestScoresByDifficulty,
                    soundEnabled = soundEnabled,
                    onSelectDifficulty = viewModel::selectDifficulty,
                    onCatTap = viewModel::cycleHomePreviewMood,
                    onToggleSound = viewModel::toggleSound,
                    onStartGame = { viewModel.startGame(selectedDifficulty) },
                    onOpenHighScores = viewModel::openHighScores
                )
            }

            AppScreen.GAME -> {
                GameScreen(
                    state = gameState,
                    bestForDifficulty = bestScoresByDifficulty[gameState.difficulty] ?: 0,
                    soundEnabled = soundEnabled,
                    defaultPlayerName = lastPlayerName,
                    onFeedCat = viewModel::feedCat,
                    onToggleSound = viewModel::toggleSound,
                    onRestartGame = { viewModel.startGame(gameState.difficulty) },
                    onSaveScore = viewModel::saveCurrentScore,
                    onOpenHighScores = viewModel::openHighScores,
                    onBackHome = viewModel::navigateHome
                )
            }

            AppScreen.HIGH_SCORES -> {
                HighScoresScreen(
                    scores = allScores,
                    onClearScores = viewModel::clearAllScores,
                    onBack = viewModel::navigateHome
                )
            }
        }
    }
}
