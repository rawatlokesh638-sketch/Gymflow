package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerPlaceholder(
  modifier: Modifier = Modifier,
  shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
  val transition = rememberInfiniteTransition(label = "shimmer")
  val translateAnim = transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmer_offset"
  )

  val shimmerColors = listOf(
    Color(0xFFE2E8F0),
    Color(0xFFF1F5F9),
    Color(0xFFE2E8F0)
  )

  val brush = Brush.linearGradient(
    colors = shimmerColors,
    start = Offset(translateAnim.value - 300f, translateAnim.value - 300f),
    end = Offset(translateAnim.value, translateAnim.value)
  )

  Box(
    modifier = modifier
      .clip(shape)
      .background(brush)
  )
}

@Composable
fun DashboardSkeletonLoading(
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top date skeleton
    ShimmerPlaceholder(
      modifier = Modifier
        .fillMaxWidth()
        .height(36.dp),
      shape = RoundedCornerShape(10.dp)
    )

    // 4 KPI cards skeleton
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ShimmerPlaceholder(
        modifier = Modifier
          .weight(1f)
          .height(130.dp),
        shape = RoundedCornerShape(16.dp)
      )
      ShimmerPlaceholder(
        modifier = Modifier
          .weight(1f)
          .height(130.dp),
        shape = RoundedCornerShape(16.dp)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ShimmerPlaceholder(
        modifier = Modifier
          .weight(1f)
          .height(130.dp),
        shape = RoundedCornerShape(16.dp)
      )
      ShimmerPlaceholder(
        modifier = Modifier
          .weight(1f)
          .height(130.dp),
        shape = RoundedCornerShape(16.dp)
      )
    }

    // 5th Card skeleton
    ShimmerPlaceholder(
      modifier = Modifier
        .fillMaxWidth()
        .height(100.dp),
      shape = RoundedCornerShape(16.dp)
    )

    // Overview skeleton
    ShimmerPlaceholder(
      modifier = Modifier
        .fillMaxWidth()
        .height(140.dp),
      shape = RoundedCornerShape(16.dp)
    )

    // List skeleton
    ShimmerPlaceholder(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp),
      shape = RoundedCornerShape(16.dp)
    )
  }
}
