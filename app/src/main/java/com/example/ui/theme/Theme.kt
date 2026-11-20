package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkGingerPrimary,
    onPrimary = DarkGingerOnPrimary,
    primaryContainer = DarkGingerPrimaryContainer,
    onPrimaryContainer = DarkGingerOnPrimaryContainer,
    secondary = DarkCatnipSecondary,
    onSecondary = DarkCatnipOnSecondary,
    secondaryContainer = DarkCatnipSecondaryContainer,
    onSecondaryContainer = DarkCatnipOnSecondaryContainer,
    tertiary = DarkBerryTertiary,
    onTertiary = DarkBerryOnTertiary,
    tertiaryContainer = DarkBerryTertiaryContainer,
    onTertiaryContainer = DarkBerryOnBerryContainer,
    background = DarkEspressoBackground,
    onBackground = DarkEspressoOnBackground,
    surface = DarkEspressoSurface,
    onSurface = DarkEspressoOnBackground,
    surfaceVariant = DarkEspressoSurfaceVariant,
    onSurfaceVariant = DarkEspressoOnSurfaceVariant,
    outline = DarkEspressoOutline
)

private val LightColorScheme = lightColorScheme(
    primary = GingerPrimary,
    onPrimary = GingerOnPrimary,
    primaryContainer = GingerPrimaryContainer,
    onPrimaryContainer = GingerOnPrimaryContainer,
    secondary = CatnipSecondary,
    onSecondary = CatnipOnSecondary,
    secondaryContainer = CatnipSecondaryContainer,
    onSecondaryContainer = CatnipOnSecondaryContainer,
    tertiary = BerryTertiary,
    onTertiary = BerryOnTertiary,
    tertiaryContainer = BerryTertiaryContainer,
    onTertiaryContainer = BerryOnTertiaryContainer,
    background = CreamBackground,
    onBackground = CreamOnBackground,
    surface = CreamSurface,
    onSurface = CreamOnBackground,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = CreamOnSurfaceVariant,
    outline = CreamOutline
)

val MeongShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MeongLaparTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MeongShapes,
        content = content
    )
}
