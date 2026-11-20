package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CatMood
import com.example.model.DifficultyLevel
import com.example.ui.components.CatPortraitStage
import com.example.ui.components.MochiBackground
import com.example.ui.theme.HappyGold

@Composable
fun HomeScreen(
    selectedDifficulty: DifficultyLevel,
    previewMood: CatMood,
    overallBestScore: Int,
    bestScoresByDifficulty: Map<DifficultyLevel, Int>,
    soundEnabled: Boolean,
    onSelectDifficulty: (DifficultyLevel) -> Unit,
    onCatTap: () -> Unit,
    onToggleSound: () -> Unit,
    onStartGame: () -> Unit,
    onOpenHighScores: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    MochiBackground(topAlpha = 0.34f, bottomAlpha = 0.16f) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.tagline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = HappyGold.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, HappyGold.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .clickable { onOpenHighScores() }
                                .testTag("header_best_score_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = stringResource(R.string.btn_high_scores),
                                    tint = Color(0xFFC87D00),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "$overallBestScore",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp),
                            tonalElevation = 2.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            IconButton(
                                onClick = onToggleSound,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("home_sound_toggle")
                            ) {
                                Icon(
                                    imageVector = if (soundEnabled) {
                                        Icons.AutoMirrored.Rounded.VolumeUp
                                    } else {
                                        Icons.AutoMirrored.Rounded.VolumeOff
                                    },
                                    contentDescription = stringResource(R.string.cd_sound_toggle),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive MOCHI Cat Hero Preview
                val previewSpeech = when (previewMood) {
                    CatMood.HUNGRY -> "Ketuk MOCHI untuk lihat ekspresi! MOCHI lapar~"
                    CatMood.HAPPY -> "Purr~ MOCHI senang banget kalau disuapin ikan!"
                    CatMood.ANGRY -> "HISSS!! MOCHI marah kalau dikasih jebakan!"
                    CatMood.GAME_OVER -> "Hiks... MOCHI nangis kalau sampai Game Over!"
                }
                CatPortraitStage(
                    mood = previewMood,
                    speechText = previewSpeech,
                    portraitSize = 228.dp,
                    onCatTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCatTap()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Difficulty Selector Section
                Text(
                    text = stringResource(R.string.title_select_difficulty),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DifficultyLevel.entries.forEach { level ->
                        DifficultyOptionCard(
                            level = level,
                            bestScore = bestScoresByDifficulty[level] ?: 0,
                            isSelected = level == selectedDifficulty,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectDifficulty(level)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = onStartGame,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedDifficulty.accentColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.btn_play)} (${selectedDifficulty.displayName})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onOpenHighScores,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_high_scores_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFD98800),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_high_scores),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        }
    }
}

@Composable
private fun DifficultyOptionCard(
    level: DifficultyLevel,
    bestScore: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) level.accentColor else MaterialTheme.colorScheme.outline,
        label = "diff_border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            level.accentColor.copy(alpha = 0.13f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "diff_bg"
    )

    val icon = when (level) {
        DifficultyLevel.EASY -> Icons.Rounded.Spa
        DifficultyLevel.NORMAL -> Icons.Rounded.Bolt
        DifficultyLevel.HARD -> Icons.Rounded.LocalFireDepartment
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        tonalElevation = if (isSelected) 3.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("difficulty_card_${level.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(level.accentColor.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = level.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = level.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.label_best),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$bestScore",
                        style = MaterialTheme.typography.labelLarge,
                        color = level.accentColor
                    )
                }
            }
        }
    }
}
