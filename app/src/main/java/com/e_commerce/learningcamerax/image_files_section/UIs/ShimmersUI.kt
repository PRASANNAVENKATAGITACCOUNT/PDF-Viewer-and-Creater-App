package com.e_commerce.learningcamerax.image_files_section.UIs

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun RectangleShimmer() {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Spacer(modifier = Modifier.width(8.dp))
            Box (
                Modifier
                    .clip(shape = RoundedCornerShape(8.dp))
                    .background(color = Color.LightGray)
                    .size(height = 120.dp, width = 120.dp)
                    .shimmerLoadingAnimation(),
            ) {
            }
            Spacer(modifier = Modifier.width(8.dp))

        }

        Spacer(modifier = Modifier.height(5.dp))
    }

}


fun Modifier.shimmerLoadingAnimation(
    color:Color=Color.White,
    widthOfShadowBrush: Int = 120,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 500,
):Modifier{
    return composed {
        val shimmerColor = listOf(
            color.copy(alpha = 0.2f),
            color.copy(alpha = 0.5f),
            color.copy(alpha = 1.0f),
            color.copy(alpha = 0.5f),
            color.copy(alpha = 0.3f),
        )

        val transition  = rememberInfiniteTransition(label = "")

        val translateAnimation=transition.animateFloat(
            initialValue = 0f,
            targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis= durationMillis,
                    easing = LinearEasing,
                ) ,
                repeatMode = RepeatMode.Restart
            ),
            label = "Shimmer Loading Animation"
        )

        this.background(
            brush = Brush.linearGradient(
                colors = shimmerColor,
                start = Offset(x = translateAnimation.value - widthOfShadowBrush, y = 0.0f),
                end = Offset(x = translateAnimation.value, y = angleOfAxisY)
            )
        )
    }
}




@Preview(showBackground = true)
@Composable
fun PreviewShimmer() {
    RectangleShimmer()
}