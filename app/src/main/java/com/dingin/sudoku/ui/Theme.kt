package com.dingin.sudoku.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dingin.sudoku.core.ThemeMode

private val Light = lightColorScheme(
    primary = Color(0xFF19594F), onPrimary = Color.White, primaryContainer = Color(0xFFD7EAE3), onPrimaryContainer = Color(0xFF133C34),
    secondary = Color(0xFF856239), secondaryContainer = Color(0xFFF4E5CA), onSecondaryContainer = Color(0xFF4B341A),
    background = Color(0xFFF6F5F0), onBackground = Color(0xFF1D302C), surface = Color(0xFFFEFDF9), onSurface = Color(0xFF1D302C),
    surfaceVariant = Color(0xFFECEFE8), onSurfaceVariant = Color(0xFF61716B), outline = Color(0xFFCAD1C8),
    error = Color(0xFFB44242), errorContainer = Color(0xFFFBE2DC)
)
private val Dark = darkColorScheme(
    primary = Color(0xFF9CD5C0), onPrimary = Color(0xFF163E34), primaryContainer = Color(0xFF234D42), onPrimaryContainer = Color(0xFFD7EAE3),
    secondary = Color(0xFFE4C395), secondaryContainer = Color(0xFF4E4030), onSecondaryContainer = Color(0xFFF4E5CA),
    background = Color(0xFF111E1A), onBackground = Color(0xFFE9EEE6), surface = Color(0xFF182823), onSurface = Color(0xFFE9EEE6),
    surfaceVariant = Color(0xFF23352E), onSurfaceVariant = Color(0xFFA4B4A8), outline = Color(0xFF455A4F),
    error = Color(0xFFFFA4A4), errorContainer = Color(0xFF592D2D)
)
@Composable fun DinginTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = mode == ThemeMode.DARK || mode == ThemeMode.SYSTEM && isSystemInDarkTheme()
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Typography(
        displaySmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 38.sp, lineHeight = 44.sp),
        headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 28.sp),
        titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
        titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    ), content = content)
}
