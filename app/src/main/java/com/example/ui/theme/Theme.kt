package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = LimeNeon,
    onPrimary = Color.Black,
    primaryContainer = MediumGrey,
    onPrimaryContainer = LimeNeon,
    
    secondary = TextSecondary,
    onSecondary = Color.White,
    
    background = BlackPure,
    onBackground = Color.White,
    
    surface = DarkGrey,
    onSurface = Color.White,
    surfaceVariant = MediumGrey,
    onSurfaceVariant = Color.White,
    
    outline = BorderGrey,
    error = ColorError,
    onError = Color.Black
)

// Elegant, modern minimalist shapes with soft corners for a premium, clean aesthetic
val SharpShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = SharpShapes,
        content = content
    )
}
