package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.FoodType

@Composable
fun FoodIllustration(
    food: FoodType,
    modifier: Modifier = Modifier,
    iconSize: Dp = 44.dp
) {
    Canvas(modifier = modifier.size(iconSize)) {
        when (food) {
            FoodType.SALMON -> drawSalmon()
            FoodType.MILK -> drawMilk()
            FoodType.SHRIMP -> drawShrimp()
            FoodType.CHICKEN -> drawChicken()
            FoodType.TUNA -> drawTunaBowl()
            FoodType.BISCUIT -> drawBiscuit()
            FoodType.CHILI -> drawChili()
            FoodType.FISHBONE -> drawFishbone()
        }
    }
}

private fun DrawScope.drawSalmon() {
    val w = size.width
    val h = size.height
    val bodyColor = Color(0xFF4BA3E3)
    val bellyColor = Color(0xFFA8DCFF)
    val outline = Color(0xFF1E3A5F)

    // Tail fin
    val tail = Path().apply {
        moveTo(w * 0.28f, h * 0.50f)
        lineTo(w * 0.08f, h * 0.28f)
        lineTo(w * 0.14f, h * 0.50f)
        lineTo(w * 0.08f, h * 0.72f)
        close()
    }
    drawPath(tail, color = Color(0xFF3484C4))
    drawPath(tail, color = outline, style = Stroke(width = w * 0.045f, join = StrokeJoin.Round))

    // Top fin
    val topFin = Path().apply {
        moveTo(w * 0.44f, h * 0.30f)
        lineTo(w * 0.56f, h * 0.15f)
        lineTo(w * 0.64f, h * 0.32f)
        close()
    }
    drawPath(topFin, color = Color(0xFF3484C4))
    drawPath(topFin, color = outline, style = Stroke(width = w * 0.04f, join = StrokeJoin.Round))

    // Body oval
    drawOval(
        color = bodyColor,
        topLeft = Offset(w * 0.22f, h * 0.27f),
        size = Size(w * 0.68f, h * 0.46f)
    )
    // Belly highlight
    drawOval(
        color = bellyColor,
        topLeft = Offset(w * 0.32f, h * 0.48f),
        size = Size(w * 0.48f, h * 0.20f)
    )
    drawOval(
        color = outline,
        topLeft = Offset(w * 0.22f, h * 0.27f),
        size = Size(w * 0.68f, h * 0.46f),
        style = Stroke(width = w * 0.05f)
    )

    // Gill & eye
    drawArc(
        color = outline,
        startAngle = -60f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(w * 0.60f, h * 0.34f),
        size = Size(w * 0.12f, h * 0.30f),
        style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
    )
    drawCircle(color = Color.White, radius = w * 0.065f, center = Offset(w * 0.76f, h * 0.44f))
    drawCircle(color = outline, radius = w * 0.035f, center = Offset(w * 0.77f, h * 0.44f))
}

private fun DrawScope.drawMilk() {
    val w = size.width
    val h = size.height
    val outline = Color(0xFF284166)

    // Carton body
    drawRoundRect(
        color = Color(0xFFFDFEFF),
        topLeft = Offset(w * 0.26f, h * 0.34f),
        size = Size(w * 0.48f, h * 0.54f),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
    )
    // Blue label band
    drawRect(
        color = Color(0xFF60B5FF),
        topLeft = Offset(w * 0.26f, h * 0.50f),
        size = Size(w * 0.48f, h * 0.20f)
    )
    // Top roof
    val roof = Path().apply {
        moveTo(w * 0.26f, h * 0.36f)
        lineTo(w * 0.38f, h * 0.16f)
        lineTo(w * 0.62f, h * 0.16f)
        lineTo(w * 0.74f, h * 0.36f)
        close()
    }
    drawPath(roof, color = Color(0xFFD6EEFF))
    drawPath(roof, color = outline, style = Stroke(width = w * 0.045f, join = StrokeJoin.Round))

    drawRoundRect(
        color = outline,
        topLeft = Offset(w * 0.26f, h * 0.34f),
        size = Size(w * 0.48f, h * 0.54f),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
        style = Stroke(width = w * 0.045f)
    )
    // Cap
    drawRoundRect(
        color = Color(0xFF2E86DE),
        topLeft = Offset(w * 0.44f, h * 0.10f),
        size = Size(w * 0.14f, h * 0.09f),
        cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
    )
    // Milk drop on label
    drawCircle(color = Color.White, radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.60f))
}

private fun DrawScope.drawShrimp() {
    val w = size.width
    val h = size.height
    val outline = Color(0xFF7A2810)

    // Tail fan
    val tail = Path().apply {
        moveTo(w * 0.32f, h * 0.76f)
        lineTo(w * 0.14f, h * 0.84f)
        lineTo(w * 0.20f, h * 0.64f)
        close()
    }
    drawPath(tail, color = Color(0xFFFF6F59))
    drawPath(tail, color = outline, style = Stroke(width = w * 0.04f, join = StrokeJoin.Round))

    // Curved shrimp arc body
    drawArc(
        color = Color(0xFFFF8C61),
        startAngle = 210f,
        sweepAngle = 230f,
        useCenter = false,
        topLeft = Offset(w * 0.22f, h * 0.20f),
        size = Size(w * 0.58f, h * 0.58f),
        style = Stroke(width = w * 0.22f, cap = StrokeCap.Round)
    )
    // Stripes
    drawArc(
        color = Color(0xFFFFD6C2),
        startAngle = 240f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(w * 0.24f, h * 0.22f),
        size = Size(w * 0.54f, h * 0.54f),
        style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
    )
    // Head circle & eye
    drawCircle(color = Color(0xFFFF7A4D), radius = w * 0.14f, center = Offset(w * 0.66f, h * 0.38f))
    drawCircle(color = outline, radius = w * 0.14f, center = Offset(w * 0.66f, h * 0.38f), style = Stroke(w * 0.04f))
    drawCircle(color = Color.Black, radius = w * 0.035f, center = Offset(w * 0.70f, h * 0.35f))
}

private fun DrawScope.drawChicken() {
    val w = size.width
    val h = size.height
    val outline = Color(0xFF5A2A0C)

    // Bone handle
    drawLine(
        color = Color(0xFFFFF7E8),
        start = Offset(w * 0.24f, h * 0.76f),
        end = Offset(w * 0.48f, h * 0.52f),
        strokeWidth = w * 0.14f,
        cap = StrokeCap.Round
    )
    drawCircle(color = Color(0xFFFFF7E8), radius = w * 0.07f, center = Offset(w * 0.20f, h * 0.72f))
    drawCircle(color = Color(0xFFFFF7E8), radius = w * 0.07f, center = Offset(w * 0.28f, h * 0.80f))

    // Drumstick meat
    drawOval(
        color = Color(0xFFD97724),
        topLeft = Offset(w * 0.36f, h * 0.18f),
        size = Size(w * 0.50f, h * 0.48f)
    )
    drawOval(
        color = Color(0xFFF5A24B),
        topLeft = Offset(w * 0.44f, h * 0.24f),
        size = Size(w * 0.26f, h * 0.20f)
    )
    drawOval(
        color = outline,
        topLeft = Offset(w * 0.36f, h * 0.18f),
        size = Size(w * 0.50f, h * 0.48f),
        style = Stroke(width = w * 0.045f)
    )
}

private fun DrawScope.drawTunaBowl() {
    val w = size.width
    val h = size.height
    val outline = Color(0xFF4A2511)

    // Tuna mound
    drawOval(
        color = Color(0xFFF28482),
        topLeft = Offset(w * 0.22f, h * 0.32f),
        size = Size(w * 0.56f, h * 0.28f)
    )
    drawOval(
        color = Color(0xFFF5CAC3),
        topLeft = Offset(w * 0.30f, h * 0.36f),
        size = Size(w * 0.26f, h * 0.10f)
    )

    // Bowl base
    val bowl = Path().apply {
        moveTo(w * 0.14f, h * 0.48f)
        lineTo(w * 0.86f, h * 0.48f)
        lineTo(w * 0.74f, h * 0.80f)
        lineTo(w * 0.26f, h * 0.80f)
        close()
    }
    drawPath(bowl, color = Color(0xFFFFB703))
    drawPath(bowl, color = outline, style = Stroke(width = w * 0.045f, join = StrokeJoin.Round))

    // Bowl rim
    drawRoundRect(
        color = Color(0xFFFB8500),
        topLeft = Offset(w * 0.12f, h * 0.44f),
        size = Size(w * 0.76f, h * 0.11f),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(w * 0.12f, h * 0.44f),
        size = Size(w * 0.76f, h * 0.11f),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
        style = Stroke(width = w * 0.04f)
    )
}

private fun DrawScope.drawBiscuit() {
    val w = size.width
    val h = size.height
    val outline = Color(0xFF6B3E10)

    // Fish-shaped golden cracker
    val tail = Path().apply {
        moveTo(w * 0.30f, h * 0.50f)
        lineTo(w * 0.12f, h * 0.32f)
        lineTo(w * 0.12f, h * 0.68f)
        close()
    }
    drawPath(tail, color = Color(0xFFF6B93B))
    drawPath(tail, color = outline, style = Stroke(width = w * 0.045f, join = StrokeJoin.Round))

    drawOval(
        color = Color(0xFFF8C755),
        topLeft = Offset(w * 0.26f, h * 0.28f),
        size = Size(w * 0.62f, h * 0.44f)
    )
    drawOval(
        color = outline,
        topLeft = Offset(w * 0.26f, h * 0.28f),
        size = Size(w * 0.62f, h * 0.44f),
        style = Stroke(width = w * 0.045f)
    )
    // Cracker dots & smile
    drawCircle(color = outline, radius = w * 0.03f, center = Offset(w * 0.46f, h * 0.45f))
    drawCircle(color = outline, radius = w * 0.03f, center = Offset(w * 0.56f, h * 0.54f))
    drawCircle(color = outline, radius = w * 0.035f, center = Offset(w * 0.72f, h * 0.44f))
}

private fun DrawScope.drawChili() {
    val w = size.width
    val h = size.height
    val outline = Color(0xFF6B0F1A)

    // Stem
    drawLine(
        color = Color(0xFF2E9B4B),
        start = Offset(w * 0.66f, h * 0.26f),
        end = Offset(w * 0.78f, h * 0.12f),
        strokeWidth = w * 0.08f,
        cap = StrokeCap.Round
    )
    // Fiery red curved chili body
    val chili = Path().apply {
        moveTo(w * 0.66f, h * 0.24f)
        quadraticTo(w * 0.80f, h * 0.60f, w * 0.22f, h * 0.82f)
        quadraticTo(w * 0.46f, h * 0.56f, w * 0.50f, h * 0.28f)
        close()
    }
    drawPath(chili, color = Color(0xFFE63946))
    drawPath(chili, color = outline, style = Stroke(width = w * 0.045f, join = StrokeJoin.Round))
    // Highlight
    drawCircle(color = Color(0xFFFF8A94), radius = w * 0.05f, center = Offset(w * 0.58f, h * 0.40f))
}

private fun DrawScope.drawFishbone() {
    val w = size.width
    val h = size.height
    val bone = Color(0xFFF3F4F6)
    val outline = Color(0xFF374151)

    // Spine
    drawLine(
        color = outline,
        start = Offset(w * 0.16f, h * 0.50f),
        end = Offset(w * 0.82f, h * 0.50f),
        strokeWidth = w * 0.08f,
        cap = StrokeCap.Round
    )
    // Ribs
    listOf(0.34f, 0.50f, 0.66f).forEach { xRatio ->
        drawLine(
            color = outline,
            start = Offset(w * xRatio, h * 0.28f),
            end = Offset(w * xRatio, h * 0.72f),
            strokeWidth = w * 0.065f,
            cap = StrokeCap.Round
        )
    }
    // Head skull triangle
    val head = Path().apply {
        moveTo(w * 0.76f, h * 0.34f)
        lineTo(w * 0.90f, h * 0.50f)
        lineTo(w * 0.76f, h * 0.66f)
        close()
    }
    drawPath(head, color = bone)
    drawPath(head, color = outline, style = Stroke(width = w * 0.04f, join = StrokeJoin.Round))

    // Tail bone
    val tail = Path().apply {
        moveTo(w * 0.22f, h * 0.50f)
        lineTo(w * 0.10f, h * 0.32f)
        lineTo(w * 0.10f, h * 0.68f)
        close()
    }
    drawPath(tail, color = bone)
    drawPath(tail, color = outline, style = Stroke(width = w * 0.04f, join = StrokeJoin.Round))
}
