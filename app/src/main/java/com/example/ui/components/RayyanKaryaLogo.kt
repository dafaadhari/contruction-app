package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CorporateBlue

@Composable
fun RayyanKaryaLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp,
    showText: Boolean = true,
    isDarkBg: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // High fidelity typographic monogram representing the corporate "R" identity
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R",
                color = CorporateBlue,
                fontSize = (iconSize.value * 0.9).sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.sp
            )
        }
        
        if (showText) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "RAYYAN KARYA",
                    color = CorporateBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "A l u m i n i u m  &  K o n s t r u k s i",
                    color = if (isDarkBg) Color.White.copy(alpha = 0.60f) else Color.DarkGray,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
