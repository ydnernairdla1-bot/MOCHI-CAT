package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.FoodType
import com.example.ui.components.CatPortraitStage
import com.example.ui.components.FoodIllustration
import com.example.ui.components.MochiBackground
import com.example.ui.theme.AngryCrimson
import com.example.ui.theme.HappyGold
import com.example.ui.theme.HappyMint
import com.example.ui.theme.PatienceAmber
import com.example.ui.theme.TrapBorder
import com.example.ui.theme.TrapTint
import com.example.viewmodel.GameUiState

@Composable
fun GameScreen(
    state: GameUiState,
    bestForDifficulty: Int,
    soundEnabled: Boolean,
    defaultPlayerName: String,
    onFeedCat: (FoodType) -> Unit,
    onToggleSound: () -> Unit,
    onRestartGame: () -> Unit,
    onSaveScore: (String) -> Unit,
    onOpenHighScores: () -> Unit,
    onBackHome: () -> Unit
) {
    BackHandler {
        onBackHome()
    }

    val haptic = LocalHapticFeedback.current
    val difficultyTint = state.difficulty.accentColor.copy(alpha = 0.12f)

    val patienceColor by animateColorAsState(
        targetValue = when {
            state.patienceProgress > 0.55f -> HappyMint
            state.patienceProgress > 0.28f -> PatienceAmber
            else -> AngryCrimson
        },
        label = "patience_color"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = state.patienceProgress,
        label = "patience_progress"
    )

    val lowPatienceInfinite = rememberInfiniteTransition(label = "low_patience")
    val lowPatiencePulse by lowPatienceInfinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "low_patience_alpha"
    )

    val flyProgress = remember { Animatable(1f) }
    LaunchedEffect(state.lastFedEvent?.id) {
        if (state.lastFedEvent != null) {
            flyProgress.snapTo(0f)
            flyProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
            )
        }
    }

    val popupAnim = remember { Animatable(1f) }
    LaunchedEffect(state.feedbackPopup?.id) {
        if (state.feedbackPopup != null) {
            popupAnim.snapTo(0f)
            popupAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
            )
        }
    }

    MochiBackground(topAlpha = 0.30f, bottomAlpha = 0.14f) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = difficultyTint,
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top HUD Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            IconButton(
                                onClick = onBackHome,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("game_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = state.difficulty.accentColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, state.difficulty.accentColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = state.difficulty.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = state.difficulty.accentColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Animated Lives Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..state.difficulty.maxLives) {
                            val alive = i <= state.lives
                            val heartScale by animateFloatAsState(
                                targetValue = if (alive) 1f else 0.78f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "heart_$i"
                            )
                            Icon(
                                imageVector = if (alive) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = null,
                                tint = if (alive) AngryCrimson else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        scaleX = heartScale
                                        scaleY = heartScale
                                    }
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        IconButton(
                            onClick = onToggleSound,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("game_sound_toggle")
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

                Spacer(modifier = Modifier.height(10.dp))

                // Score, Best, Combo Banner
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.label_score),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${state.score}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("game_score_text")
                            )
                        }

                        AnimatedVisibility(
                            visible = state.combo >= 2,
                            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Surface(
                                color = HappyGold.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.5.dp, HappyGold)
                            ) {
                                Text(
                                    text = "KOMBO x${state.combo}!",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFFA66000),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.label_best),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${maxOf(bestForDifficulty, state.score)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Patience Meter with low-patience urgency pulse
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.label_patience),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.1f dtk", state.patienceRemainingMs / 1000f),
                            style = MaterialTheme.typography.labelMedium,
                            color = patienceColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .graphicsLayer {
                                alpha = if (state.patienceProgress < 0.30f && !state.isGameOver) {
                                    lowPatiencePulse
                                } else {
                                    1f
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(50))
                                .background(patienceColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // MOCHI Stage + Flying Food Treat + Animated Floating Feedback Popup
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CatPortraitStage(
                        mood = state.mood,
                        speechText = state.speechText,
                        targetFood = state.targetFood,
                        portraitSize = 214.dp
                    )

                    // Flying food treat launching upward into MOCHI
                    state.lastFedEvent?.let { event ->
                        val p = flyProgress.value
                        if (p < 0.99f) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .graphicsLayer {
                                        translationY = -p * 260f
                                        val s = 1.25f - (p * 0.55f)
                                        scaleX = s
                                        scaleY = s
                                        rotationZ = if (event.isPositive) p * 18f else -p * 32f
                                        alpha = (1f - p * 0.85f).coerceIn(0f, 1f)
                                    }
                            ) {
                                FoodIllustration(
                                    food = event.food,
                                    iconSize = 54.dp
                                )
                            }
                        }
                    }

                    // Animated floating score / penalty popup
                    state.feedbackPopup?.let { popup ->
                        val p = popupAnim.value
                        if (p < 0.99f) {
                            Surface(
                                color = if (popup.isPositive) HappyMint else AngryCrimson,
                                shape = RoundedCornerShape(50),
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 32.dp, end = 6.dp)
                                    .graphicsLayer {
                                        translationY = -p * 56f
                                        val popScale = if (p < 0.25f) 0.75f + (p / 0.25f) * 0.35f else 1.1f - (p * 0.1f)
                                        scaleX = popScale
                                        scaleY = popScale
                                        alpha = if (p > 0.65f) (1f - (p - 0.65f) / 0.35f).coerceIn(0f, 1f) else 1f
                                    }
                            ) {
                                Text(
                                    text = popup.message,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Food Tray Grid
                val columns = if (state.trayItems.size <= 4) 2 else 3
                val rows = state.trayItems.chunked(columns)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                ) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { food ->
                                FoodTrayCard(
                                    food = food,
                                    onClick = {
                                        val isGood = food == state.targetFood && !food.isTrap
                                        haptic.performHapticFeedback(
                                            if (isGood) HapticFeedbackType.TextHandleMove else HapticFeedbackType.LongPress
                                        )
                                        onFeedCat(food)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }

    if (state.isGameOver) {
        GameOverDialog(
            state = state,
            defaultPlayerName = defaultPlayerName,
            onSaveScore = onSaveScore,
            onPlayAgain = onRestartGame,
            onOpenHighScores = onOpenHighScores,
            onBackHome = onBackHome
        )
    }
}

@Composable
private fun FoodTrayCard(
    food: FoodType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "food_press_scale"
    )

    val bgColor = if (food.isTrap) TrapTint else MaterialTheme.colorScheme.surface
    val borderColor = if (food.isTrap) TrapBorder else MaterialTheme.colorScheme.outline

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .testTag("food_card_${food.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                FoodIllustration(
                    food = food,
                    iconSize = 42.dp
                )
                if (food.isTrap) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = AngryCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = food.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = if (food.isTrap) AngryCrimson else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GameOverDialog(
    state: GameUiState,
    defaultPlayerName: String,
    onSaveScore: (String) -> Unit,
    onPlayAgain: () -> Unit,
    onOpenHighScores: () -> Unit,
    onBackHome: () -> Unit
) {
    var playerName by remember(defaultPlayerName) { mutableStateOf(defaultPlayerName) }
    val tearBlue = Color(0xFF4A6CF7)
    val sobInfinite = rememberInfiniteTransition(label = "gameover_sob")
    val sobBob by sobInfinite.animateFloat(
        initialValue = -2.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sob_bob"
    )
    val tearFall by sobInfinite.animateFloat(
        initialValue = 0f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tear_fall"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            border = BorderStroke(2.dp, tearBlue.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isNewRecord) {
                    Surface(
                        color = HappyGold.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.5.dp, HappyGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFC87D00),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.new_record_badge),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFA66000)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Animated Crying MOCHI Game Over Expression
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(152.dp)
                        .graphicsLayer {
                            translationY = sobBob
                            rotationZ = sobBob * 0.6f
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(134.dp)
                            .clip(CircleShape)
                            .border(4.dp, tearBlue, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cat_gameover),
                            contentDescription = stringResource(R.string.cd_cat_portrait),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint = tearBlue,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = (10 + tearFall).dp)
                            .size(26.dp)
                    )
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint = tearBlue,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 4.dp, y = (16 + ((tearFall + 8f) % 16f)).dp)
                            .size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.game_over_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = tearBlue,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Level ${state.difficulty.displayName} • ${state.foodsFed} makanan berhasil disuap",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.label_score),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${state.score}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.label_combo),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "x${state.bestCombo}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                if (state.score > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = playerName,
                        onValueChange = { if (it.length <= 24) playerName = it },
                        label = { Text(stringResource(R.string.player_name_hint)) },
                        singleLine = true,
                        enabled = !state.scoreSaved,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onSaveScore(playerName) },
                        enabled = !state.scoreSaved,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HappyMint,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_score_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.scoreSaved) "Skor Tersimpan!" else stringResource(R.string.btn_save_score),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onPlayAgain,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("play_again_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.btn_play_again),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenHighScores,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gameover_highscores_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Skor", maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = onBackHome,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gameover_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Menu", maxLines = 1)
                    }
                }
            }
        }
    }
}
