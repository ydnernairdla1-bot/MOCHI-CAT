package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoodBad
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CatMood
import com.example.model.FoodType
import com.example.ui.theme.AngryCrimson
import com.example.ui.theme.HappyGold
import com.example.ui.theme.HappyMint
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CatPortraitStage(
    mood: CatMood,
    speechText: String,
    targetFood: FoodType? = null,
    portraitSize: Dp = 224.dp,
    onCatTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "mochi_living_anim")

    // Organic continuous breathing & swaying for HUNGRY curiosity
    val breathPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath_phase"
    )

    // Continuous spinning aura / sunbeams
    val auraSpin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_spin"
    )

    // Continuous rising particle cycle
    val particleCycle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_cycle"
    )

    // Joyful dance bounce cycle for HAPPY
    val happyBouncePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(620, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "happy_bounce"
    )

    // Multi-frequency jitter phase for ANGRY rage tremor
    val angryJitterPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (6 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(260, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angry_jitter"
    )

    // Floating bob for craving food icon
    val cravingBob by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "craving_bob"
    )

    // One-shot elastic squash & stretch + shockwave burst when mood changes!
    val impactSquash = remember { Animatable(0f) }
    val shockwaveBurst = remember { Animatable(1f) }
    LaunchedEffect(mood) {
        impactSquash.snapTo(1f)
        impactSquash.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }
    LaunchedEffect(mood) {
        if (mood == CatMood.HAPPY || mood == CatMood.ANGRY) {
            shockwaveBurst.snapTo(0f)
            shockwaveBurst.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        } else {
            shockwaveBurst.snapTo(1f)
        }
    }

    val tearBlue = Color(0xFF4A6CF7)
    val ringColor by animateColorAsState(
        targetValue = when (mood) {
            CatMood.HAPPY -> HappyMint
            CatMood.ANGRY -> AngryCrimson
            CatMood.HUNGRY -> MaterialTheme.colorScheme.primary
            CatMood.GAME_OVER -> tearBlue
        },
        animationSpec = tween(200),
        label = "ring_color"
    )

    // Compute lively organic transform values
    val impact = impactSquash.value
    val breathVal = sin(breathPhase)
    val bounceSin = sin(happyBouncePhase)
    val bounceAbs = abs(bounceSin)

    val translationX = when (mood) {
        CatMood.ANGRY -> (sin(angryJitterPhase) * 14f) + (cos(angryJitterPhase * 1.7f) * 6f)
        CatMood.HAPPY -> sin(happyBouncePhase * 0.5f) * 5f
        CatMood.HUNGRY -> sin(breathPhase * 0.5f) * 3f
        CatMood.GAME_OVER -> sin(angryJitterPhase * 0.6f) * 3.5f
    }

    val translationY = when (mood) {
        CatMood.HAPPY -> -bounceAbs * 22f
        CatMood.ANGRY -> cos(angryJitterPhase * 1.3f) * 6f
        CatMood.HUNGRY -> breathVal * 5f
        CatMood.GAME_OVER -> abs(sin(breathPhase * 1.6f)) * 7f
    }

    val rotationZ = when (mood) {
        CatMood.HAPPY -> bounceSin * 6.5f
        CatMood.ANGRY -> sin(angryJitterPhase * 0.9f) * 4.5f
        CatMood.HUNGRY -> breathVal * 3.2f
        CatMood.GAME_OVER -> -3.5f + sin(breathPhase * 1.6f) * 2.5f
    }

    val rotationY = when (mood) {
        CatMood.HAPPY -> bounceSin * 7f
        CatMood.HUNGRY -> cos(breathPhase * 0.5f) * 5f
        CatMood.ANGRY, CatMood.GAME_OVER -> 0f
    }

    // Squash-and-stretch scale combining continuous life + elastic transition impulse
    val baseScaleX = when (mood) {
        CatMood.HAPPY -> 1.04f + (1f - bounceAbs) * 0.06f + impact * 0.14f
        CatMood.ANGRY -> 1.07f + abs(sin(angryJitterPhase * 0.5f)) * 0.04f + impact * 0.12f
        CatMood.HUNGRY -> 1.01f + breathVal * 0.025f - impact * 0.05f
        CatMood.GAME_OVER -> 1.0f + abs(sin(breathPhase * 1.6f)) * 0.03f
    }
    val baseScaleY = when (mood) {
        CatMood.HAPPY -> 1.04f + bounceAbs * 0.07f - impact * 0.10f
        CatMood.ANGRY -> 1.07f + abs(cos(angryJitterPhase * 0.5f)) * 0.04f + impact * 0.12f
        CatMood.HUNGRY -> 1.01f - breathVal * 0.02f + impact * 0.06f
        CatMood.GAME_OVER -> 0.99f - abs(sin(breathPhase * 1.6f)) * 0.03f
    }

    // Subtle inner photo breathing/zoom so MOCHI inside the frame looks alive!
    val innerPhotoScale = when (mood) {
        CatMood.HUNGRY -> 1.03f + breathVal * 0.025f
        CatMood.HAPPY -> 1.04f + bounceAbs * 0.03f
        CatMood.ANGRY -> 1.08f + abs(sin(angryJitterPhase * 0.5f)) * 0.04f
        CatMood.GAME_OVER -> 1.04f + abs(sin(breathPhase * 1.6f)) * 0.03f
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bouncy MOCHI Name + Mood Status Pill
        Surface(
            color = ringColor.copy(alpha = 0.16f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.5.dp, ringColor.copy(alpha = 0.65f)),
            modifier = Modifier.graphicsLayer {
                val pillPop = 1f + impact * 0.14f
                scaleX = pillPop
                scaleY = pillPop
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when (mood) {
                        CatMood.HAPPY -> Icons.Rounded.SentimentVerySatisfied
                        CatMood.ANGRY -> Icons.Rounded.MoodBad
                        CatMood.HUNGRY -> Icons.Rounded.Pets
                        CatMood.GAME_OVER -> Icons.Rounded.SentimentDissatisfied
                    },
                    contentDescription = null,
                    tint = ringColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = when (mood) {
                        CatMood.HAPPY -> "${stringResource(R.string.cat_name)} • ${stringResource(R.string.mood_happy)}!"
                        CatMood.ANGRY -> "${stringResource(R.string.cat_name)} • ${stringResource(R.string.mood_angry)}!"
                        CatMood.HUNGRY -> "${stringResource(R.string.cat_name)} • ${stringResource(R.string.mood_hungry)}"
                        CatMood.GAME_OVER -> "${stringResource(R.string.cat_name)} • Game Over"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = ringColor
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // MOCHI Portrait Stage + Dynamic Shockwaves, Sunbeams, Steam & Confetti
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(portraitSize + 44.dp)
                .graphicsLayer {
                    this.translationX = translationX
                    this.translationY = translationY
                    this.rotationZ = rotationZ
                    this.rotationY = rotationY
                    this.cameraDistance = 14f * density
                    this.scaleX = baseScaleX
                    this.scaleY = baseScaleY
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.minDimension * 0.41f

                // Soft radial mood glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.42f),
                            ringColor.copy(alpha = 0.0f)
                        ),
                        center = center,
                        radius = size.minDimension * 0.50f
                    ),
                    radius = size.minDimension * 0.50f,
                    center = center
                )

                // Expanding shockwave ring on HAPPY / ANGRY mood trigger
                val burst = shockwaveBurst.value
                if (burst < 0.98f) {
                    val waveRadius = baseRadius + burst * 34.dp.toPx()
                    drawCircle(
                        color = ringColor.copy(alpha = (1f - burst) * 0.75f),
                        radius = waveRadius,
                        center = center,
                        style = Stroke(width = (5f * (1f - burst)).dp.toPx())
                    )
                }

                // Rotating sunbeams behind MOCHI when HAPPY
                if (mood == CatMood.HAPPY) {
                    rotate(degrees = auraSpin, pivot = center) {
                        for (i in 0 until 10) {
                            val angle = Math.toRadians(i * 36.0)
                            val r1 = baseRadius + 4.dp.toPx()
                            val r2 = baseRadius + 22.dp.toPx()
                            drawLine(
                                color = HappyGold.copy(alpha = 0.45f),
                                start = Offset(
                                    center.x + (cos(angle) * r1).toFloat(),
                                    center.y + (sin(angle) * r1).toFloat()
                                ),
                                end = Offset(
                                    center.x + (cos(angle) * r2).toFloat(),
                                    center.y + (sin(angle) * r2).toFloat()
                                ),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Spinning dashed energy ring
                rotate(degrees = if (mood == CatMood.ANGRY) -auraSpin * 2.2f else auraSpin, pivot = center) {
                    drawCircle(
                        color = ringColor.copy(alpha = 0.52f),
                        radius = baseRadius + 7.dp.toPx(),
                        center = center,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                        )
                    )
                }

                // Continuous rising particles around MOCHI
                when (mood) {
                    CatMood.HAPPY -> {
                        val angles = listOf(-145.0, -110.0, -70.0, -35.0, 15.0, 165.0)
                        angles.forEachIndexed { idx, deg ->
                            val phase = (particleCycle + idx * 0.17f) % 1f
                            val rad = Math.toRadians(deg)
                            val dist = baseRadius + 4.dp.toPx() + (phase * 26.dp.toPx())
                            val px = center.x + (cos(rad) * dist).toFloat()
                            val py = center.y + (sin(rad) * dist).toFloat() - (phase * 18.dp.toPx())
                            val alpha = (1f - phase).coerceIn(0f, 1f)
                            val particleColor = when (idx % 3) {
                                0 -> HappyGold
                                1 -> Color(0xFFFF4D79)
                                else -> HappyMint
                            }.copy(alpha = alpha)
                            drawSparkle(
                                center = Offset(px, py),
                                radius = 7.5.dp.toPx() * (0.55f + 0.45f * (1f - phase)),
                                color = particleColor
                            )
                        }
                    }

                    CatMood.ANGRY -> {
                        // Fiery jagged shock spikes around portrait
                        for (i in 0 until 10) {
                            val angle = Math.toRadians(i * 36.0 + auraSpin * 0.8)
                            val inner = baseRadius + 3.dp.toPx()
                            val spike = 7.dp.toPx() + (10.dp.toPx() * abs(sin(angryJitterPhase * 0.6f + i)))
                            val outer = inner + spike
                            drawLine(
                                color = AngryCrimson.copy(alpha = 0.72f),
                                start = Offset(
                                    center.x + (cos(angle) * inner).toFloat(),
                                    center.y + (sin(angle) * inner).toFloat()
                                ),
                                end = Offset(
                                    center.x + (cos(angle) * outer).toFloat(),
                                    center.y + (sin(angle) * outer).toFloat()
                                ),
                                strokeWidth = 3.2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                        // Rising angry steam puffs near top-left & top-right edges
                        listOf(-128.0, -52.0).forEachIndexed { idx, deg ->
                            val p = (particleCycle + idx * 0.45f) % 1f
                            val rad = Math.toRadians(deg)
                            val dist = baseRadius + 8.dp.toPx() + p * 18.dp.toPx()
                            val px = center.x + (cos(rad) * dist).toFloat()
                            val py = center.y + (sin(rad) * dist).toFloat() - p * 14.dp.toPx()
                            drawCircle(
                                color = Color.White.copy(alpha = (1f - p) * 0.78f),
                                radius = (6f + p * 6f).dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }

                    CatMood.HUNGRY -> {
                        // Gentle floating sparkles showing playful curiosity
                        listOf(-135.0, -45.0).forEachIndexed { idx, deg ->
                            val p = (particleCycle + idx * 0.5f) % 1f
                            val rad = Math.toRadians(deg)
                            val dist = baseRadius + 8.dp.toPx() + p * 10.dp.toPx()
                            val px = center.x + (cos(rad) * dist).toFloat()
                            val py = center.y + (sin(rad) * dist).toFloat() - p * 8.dp.toPx()
                            drawSparkle(
                                center = Offset(px, py),
                                radius = 5.dp.toPx() * sin(p * Math.PI).toFloat(),
                                color = ringColor.copy(alpha = sin(p * Math.PI).toFloat() * 0.75f)
                            )
                        }
                    }

                    CatMood.GAME_OVER -> {
                        // Falling blue teardrop particles around MOCHI
                        listOf(-145.0, -35.0, 150.0, 30.0).forEachIndexed { idx, deg ->
                            val p = (particleCycle + idx * 0.25f) % 1f
                            val rad = Math.toRadians(deg)
                            val dist = baseRadius + 10.dp.toPx()
                            val px = center.x + (cos(rad) * dist).toFloat()
                            val py = center.y + (sin(rad) * dist).toFloat() + (p * 26.dp.toPx())
                            drawCircle(
                                color = tearBlue.copy(alpha = (1f - p) * 0.8f),
                                radius = 4.5.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }

            // Main photo frame
            val interactionSource = remember { MutableInteractionSource() }
            val clickableModifier = if (onCatTap != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onCatTap
                )
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .size(portraitSize)
                    .clip(CircleShape)
                    .border(5.dp, ringColor, CircleShape)
                    .then(clickableModifier)
            ) {
                AnimatedContent(
                    targetState = mood,
                    transitionSpec = {
                        (scaleIn(
                            initialScale = 0.82f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn(tween(140))) togetherWith
                            (scaleOut(targetScale = 1.12f) + fadeOut(tween(110)))
                    },
                    label = "mochi_expression_swap"
                ) { currentMood ->
                    val drawableRes = when (currentMood) {
                        CatMood.HAPPY -> R.drawable.cat_happy
                        CatMood.ANGRY -> R.drawable.cat_angry
                        CatMood.HUNGRY -> R.drawable.cat_hungry
                        CatMood.GAME_OVER -> R.drawable.cat_gameover
                    }
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = stringResource(R.string.cd_cat_portrait),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = innerPhotoScale
                                scaleY = innerPhotoScale
                            }
                    )
                }
            }

            // Floating animated reaction badges outside the portrait circle
            if (mood == CatMood.HAPPY) {
                val floatY = (sin(happyBouncePhase) * 11f).roundToInt()
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF4D6D),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(-8, floatY) }
                        .size(36.dp)
                        .graphicsLayer {
                            this.rotationZ = sin(happyBouncePhase) * 14f
                        }
                )
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = HappyGold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset { IntOffset(8, -floatY) }
                        .size(34.dp)
                        .graphicsLayer {
                            this.rotationZ = -sin(happyBouncePhase) * 18f
                        }
                )
            } else if (mood == CatMood.ANGRY) {
                val shakeX = (sin(angryJitterPhase) * 5f).roundToInt()
                val shakeY = (cos(angryJitterPhase * 1.4f) * 4f).roundToInt()
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = AngryCrimson,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(-6 + shakeX, 4 + shakeY) }
                        .size(36.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = Color(0xFFFF8800),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset { IntOffset(6 - shakeX, 6 - shakeY) }
                        .size(32.dp)
                )
            } else if (mood == CatMood.GAME_OVER) {
                val tearDropY = (particleCycle * 18f).roundToInt()
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    tint = tearBlue,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(-10, 12 + tearDropY) }
                        .size(30.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    tint = tearBlue,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset { IntOffset(10, 16 + ((tearDropY + 9) % 18)) }
                        .size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Speech & Craving Bubble
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                width = 1.5.dp,
                color = ringColor.copy(alpha = 0.55f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .graphicsLayer {
                    val bubblePop = 1f + impact * 0.05f
                    scaleX = bubblePop
                    scaleY = bubblePop
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (targetFood != null) {
                    AnimatedContent(
                        targetState = targetFood,
                        transitionSpec = {
                            (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                                (scaleOut() + fadeOut())
                        },
                        label = "craving_food_anim"
                    ) { food ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.graphicsLayer {
                                this.translationY = cravingBob
                            }
                        ) {
                            FoodIllustration(
                                food = food,
                                iconSize = 44.dp,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(
                    modifier = if (targetFood != null) Modifier.weight(1f) else Modifier,
                    horizontalAlignment = if (targetFood != null) Alignment.Start else Alignment.CenterHorizontally
                ) {
                    if (targetFood != null) {
                        Text(
                            text = stringResource(R.string.label_craving),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = speechText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (mood == CatMood.ANGRY) AngryCrimson else MaterialTheme.colorScheme.onSurface,
                        textAlign = if (targetFood != null) TextAlign.Start else TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color = color)
}
