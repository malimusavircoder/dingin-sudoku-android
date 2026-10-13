package com.dingin.sudoku.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dingin.sudoku.R

/** The same authored vector used by the adaptive launcher, never a stock grid icon. */
@Composable fun BrandMark(modifier: Modifier = Modifier) {
    Surface(color = Color(0xFF19594F), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        // Recreate the painter when the theme changes, including its cached drawing layer.
        key(MaterialTheme.colorScheme.background) {
            Image(painterResource(R.drawable.ic_dingin_foreground), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
    }
}
