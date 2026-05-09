package io.github.hugo1120.koreaderremote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF32564D),
    onPrimary = Color(0xFFF9F5EC),
    primaryContainer = Color(0xFFD7E5DC),
    onPrimaryContainer = Color(0xFF1C342E),
    secondary = Color(0xFF9B6A3A),
    onSecondary = Color(0xFFFFF8F0),
    secondaryContainer = Color(0xFFF2E0CB),
    onSecondaryContainer = Color(0xFF4A2D11),
    tertiary = Color(0xFF6C7A52),
    background = Color(0xFFF3EDE3),
    onBackground = Color(0xFF1D1A16),
    surface = Color(0xFFFFFAF3),
    onSurface = Color(0xFF1F1C18),
    surfaceVariant = Color(0xFFE6DED1),
    onSurfaceVariant = Color(0xFF5F584F),
    outline = Color(0xFFA79B8C),
    error = Color(0xFFB55E4A),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC5D7CC),
    onPrimary = Color(0xFF1E3832),
    primaryContainer = Color(0xFF294740),
    onPrimaryContainer = Color(0xFFE3F0E8),
    secondary = Color(0xFFE3C39F),
    onSecondary = Color(0xFF4C2E12),
    secondaryContainer = Color(0xFF6A4620),
    onSecondaryContainer = Color(0xFFFCE4C7),
    tertiary = Color(0xFFC7D3AF),
    background = Color(0xFF121713),
    onBackground = Color(0xFFEAE2D7),
    surface = Color(0xFF1A201C),
    onSurface = Color(0xFFEEE6DB),
    surfaceVariant = Color(0xFF303831),
    onSurfaceVariant = Color(0xFFC9C1B5),
    outline = Color(0xFF8A8174),
    error = Color(0xFFFFB4A3),
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 30.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
)

private val AppShapes = Shapes()

@Composable
fun KOReaderRemoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
