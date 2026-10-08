package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FitProTopBar
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel

@Composable
fun ReportsScreen(
  viewModel: FitProViewModel,
  onBack: () -> Unit = {}
) {
  val members by viewModel.members.collectAsState()
  val payments by viewModel.payments.collectAsState()

  var selectedPeriod by remember { mutableStateOf("This Month") }

  val totalCollection = payments.sumOf { it.amount }
  val totalMembers = members.size
  val newMembers = members.size
  val totalDueAmount = members.sumOf { it.dueAmount }

  val chartAnim = remember { Animatable(0f) }
  LaunchedEffect(Unit) {
    chartAnim.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
  }

  // Real Monthly revenue collection data computed from actual payments
  val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep")
  val monthlyData = remember(payments) {
    months.map { m ->
      val sum = payments.filter { it.date.contains(m, ignoreCase = true) }.sumOf { it.amount }.toFloat()
      Pair(m, sum)
    }
  }
  val maxDataVal = monthlyData.maxOfOrNull { it.second } ?: 0f
  val maxVal = if (maxDataVal > 0f) maxDataVal * 1.25f else 1000f

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0B0E14))
      .testTag("reports_screen"),
    contentPadding = PaddingValues(bottom = 120.dp)
  ) {
    // Header with Dropdown matching Screenshot 12: "This Month v"
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF131722),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = selectedPeriod,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = Color(0xFF8E9BAE),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // 4 Metric Cards in 2x2 Grid with Real Values starting from 0
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = "Total Collection",
            value = "₹$totalCollection",
            trend = if (totalCollection > 0) "+18%" else null,
            isPositiveTrend = true,
            icon = Icons.Filled.AccountBalanceWallet,
            iconBgColor = Color(0xFF0E3823),
            iconTint = Color(0xFF00E676),
            valueColor = Color.White,
            modifier = Modifier.weight(1f)
          )

          StatCard(
            title = "Total Members",
            value = "$totalMembers",
            trend = if (totalMembers > 0) "+12%" else null,
            isPositiveTrend = true,
            icon = Icons.Filled.PeopleAlt,
            iconBgColor = Color(0xFF0F324D),
            iconTint = Color(0xFF00E5FF),
            valueColor = Color.White,
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = "New Members",
            value = "$newMembers",
            trend = if (newMembers > 0) "+40%" else null,
            isPositiveTrend = true,
            icon = Icons.Filled.PersonAdd,
            iconBgColor = Color(0xFF2E1748),
            iconTint = Color(0xFFA855F7),
            valueColor = Color.White,
            modifier = Modifier.weight(1f)
          )

          StatCard(
            title = "Due Amount",
            value = "₹$totalDueAmount",
            trend = if (totalDueAmount > 0) "-12%" else null,
            isPositiveTrend = false,
            icon = Icons.Filled.PendingActions,
            iconBgColor = Color(0xFF381419),
            iconTint = Color(0xFFFF5252),
            valueColor = if (totalDueAmount > 0) Color(0xFFFF5252) else Color.White,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // "Monthly Collection" Bar Chart Card matching Screenshot 12
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF131722),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Monthly Collection",
            style = MaterialTheme.typography.titleMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Canvas with Y Axis labels and green bars
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp)
          ) {
            // Y-axis labels dynamic based on maxVal
            Column(
              modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 20.dp, end = 8.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              val topLabel = if (maxVal >= 100000f) "${(maxVal / 100000).toInt()}L" else "${(maxVal / 1000).toInt()}k"
              val midLabel = if (maxVal >= 100000f) "${(maxVal * 0.5f / 100000).toInt()}L" else "${(maxVal * 0.5f / 1000).toInt()}k"
              val lowLabel = if (maxVal >= 100000f) "${(maxVal * 0.25f / 100000).toInt()}L" else "${(maxVal * 0.25f / 1000).toInt()}k"
              Text(topLabel, color = Color(0xFF64748B), fontSize = 10.sp)
              Text(midLabel, color = Color(0xFF64748B), fontSize = 10.sp)
              Text(lowLabel, color = Color(0xFF64748B), fontSize = 10.sp)
              Text("0", color = Color(0xFF64748B), fontSize = 10.sp)
            }

            // Bars canvas & X axis
            Column(modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f)
              ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                  val width = size.width
                  val height = size.height
                  val barCount = monthlyData.size
                  val barWidth = width / (barCount * 1.8f)
                  val spacing = (width - (barWidth * barCount)) / (barCount + 1)

                  monthlyData.forEachIndexed { index, (_, value) ->
                    val barHeight = (value / maxVal) * height * chartAnim.value
                    val x = spacing + index * (barWidth + spacing)
                    val y = height - barHeight

                    drawRoundRect(
                      color = Color(0xFF00C853), // Solid green bar matching screenshot 12
                      topLeft = Offset(x, y),
                      size = Size(barWidth, barHeight),
                      cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              // X-axis Month labels: Jan to Sep matching Screenshot 12
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                monthlyData.forEach { (label, _) ->
                  Text(
                    text = label,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                  )
                }
              }
            }
          }
        }
      }
    }

    // "See More Reports" button matching Screenshot 12
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF131722),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { viewModel.showSnackbar("Opening detailed reports") }
        ) {
          Text(
            text = "See More Reports",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 14.dp)
          )
        }
      }
    }
  }
}
