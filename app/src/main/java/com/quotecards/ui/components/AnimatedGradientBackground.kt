package com.quotecards.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.quotecards.ui.theme.GradientBlobDark1
import com.quotecards.ui.theme.GradientBlobDark2
import com.quotecards.ui.theme.GradientBlobDark3
import com.quotecards.ui.theme.GradientBlobDark4
import com.quotecards.ui.theme.GradientBlobLight1
import com.quotecards.ui.theme.GradientBlobLight2
import com.quotecards.ui.theme.GradientBlobLight3
import com.quotecards.ui.theme.GradientBlobLight4
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Get theme-appropriate colors
    val blobColors = if (isDarkTheme) {
        listOf(GradientBlobDark1, GradientBlobDark2, GradientBlobDark3, GradientBlobDark4)
    } else {
        listOf(GradientBlobLight1, GradientBlobLight2, GradientBlobLight3, GradientBlobLight4)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "gradient_transition")

    // Animate each blob with different durations for organic feel
    val blob1Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob1_angle"
    )

    val blob2Angle by infiniteTransition.animateFloat(
        initialValue = 90f,
        targetValue = 450f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob2_angle"
    )

    val blob3Angle by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob3_angle"
    )

    val blob4Angle by infiniteTransition.animateFloat(
        initialValue = 270f,
        targetValue = 630f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob4_angle"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            // Elliptical orbit radii for each blob
            val orbitRadiusX1 = width * 0.3f
            val orbitRadiusY1 = height * 0.2f
            val orbitRadiusX2 = width * 0.25f
            val orbitRadiusY2 = height * 0.25f
            val orbitRadiusX3 = width * 0.35f
            val orbitRadiusY3 = height * 0.15f
            val orbitRadiusX4 = width * 0.2f
            val orbitRadiusY4 = height * 0.3f

            // Calculate blob positions
            val blob1X = centerX + orbitRadiusX1 * cos(Math.toRadians(blob1Angle.toDouble())).toFloat()
            val blob1Y = centerY + orbitRadiusY1 * sin(Math.toRadians(blob1Angle.toDouble())).toFloat()

            val blob2X = centerX + orbitRadiusX2 * cos(Math.toRadians(blob2Angle.toDouble())).toFloat()
            val blob2Y = centerY + orbitRadiusY2 * sin(Math.toRadians(blob2Angle.toDouble())).toFloat()

            val blob3X = centerX + orbitRadiusX3 * cos(Math.toRadians(blob3Angle.toDouble())).toFloat()
            val blob3Y = centerY + orbitRadiusY3 * sin(Math.toRadians(blob3Angle.toDouble())).toFloat()

            val blob4X = centerX + orbitRadiusX4 * cos(Math.toRadians(blob4Angle.toDouble())).toFloat()
            val blob4Y = centerY + orbitRadiusY4 * sin(Math.toRadians(blob4Angle.toDouble())).toFloat()

            // Blob sizes
            val blobRadius = minOf(width, height) * 0.6f

            // Draw each blob as a radial gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blobColors[0], Color.Transparent),
                    center = Offset(blob1X, blob1Y),
                    radius = blobRadius
                ),
                radius = blobRadius,
                center = Offset(blob1X, blob1Y)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blobColors[1], Color.Transparent),
                    center = Offset(blob2X, blob2Y),
                    radius = blobRadius
                ),
                radius = blobRadius,
                center = Offset(blob2X, blob2Y)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blobColors[2], Color.Transparent),
                    center = Offset(blob3X, blob3Y),
                    radius = blobRadius * 0.8f
                ),
                radius = blobRadius * 0.8f,
                center = Offset(blob3X, blob3Y)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blobColors[3], Color.Transparent),
                    center = Offset(blob4X, blob4Y),
                    radius = blobRadius * 0.9f
                ),
                radius = blobRadius * 0.9f,
                center = Offset(blob4X, blob4Y)
            )
        }

        // Content on top of the gradient
        content()
    }
}
