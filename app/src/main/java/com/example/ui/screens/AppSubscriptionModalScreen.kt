package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSubscriptionPlan
import com.example.ui.components.FitProTopBar
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel

@Composable
fun AppSubscriptionModalScreen(
  viewModel: FitProViewModel,
  onBack: () -> Unit
) {
  val subscriptions by viewModel.appSubscriptions.collectAsState()
  val language by viewModel.language.collectAsState()

  var selectedPlanId by remember { mutableStateOf("sub_annual") }

  Scaffold(
    containerColor = FitProBackground,
    topBar = {
      FitProTopBar(
        title = if (language == "hi") "FitPro प्रीमियम (किफायती प्लान)" else "FitPro Pro Membership",
        subtitle = "Affordable Gym Software Subscriptions",
        showBack = true,
        onBackClick = onBack
      )
    },
    bottomBar = {
      Surface(
        color = FitProBackground,
        tonalElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(16.dp)
      ) {
        val selected = subscriptions.find { it.id == selectedPlanId } ?: subscriptions.first()
        Button(
          onClick = {
            viewModel.showSnackbar("✓ Subscribed to ${selected.name} (₹${selected.totalCost})! Thank you for supporting FitPro.")
            onBack()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = FitProPrimary,
            contentColor = FitProOnPrimary
          ),
          shape = RoundedCornerShape(26.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("subscribe_confirm_button")
        ) {
          Text(
            text = "Activate ${selected.name} (₹${selected.totalCost})",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          )
        }
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(bottom = 30.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Big Special Discount Hero Card
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
              Brush.linearGradient(
                listOf(Color(0xFF0F2B1D), Color(0xFF0D1F2D))
              )
            )
            .border(1.dp, FitProPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(18.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = FitProPrimary
              ) {
                Text(
                  text = "70% OFF SPECIAL",
                  color = FitProOnPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
              Text(
                text = "Limited Time Offer",
                color = FitProTextMuted,
                fontSize = 11.sp
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = if (language == "hi") "देसी जिम मालिकों के लिए सबसे सस्ती दरें" else "Gym Owners Pocket-Friendly Pricing",
              style = MaterialTheme.typography.titleLarge.copy(
                color = FitProTextPrimary,
                fontWeight = FontWeight.ExtraBold
              )
            )

            Text(
              text = if (language == "hi") "एक मेंबर की फीस से भी कम में पूरे साल का जिम मैनेजमेंट सॉफ्टवेयर!" else "Less than the price of a single member's monthly fee!",
              style = MaterialTheme.typography.bodyMedium.copy(
                color = FitProPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              ),
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }

      // Subscription Tiers
      items(subscriptions) { plan ->
        val isSelected = selectedPlanId == plan.id
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = if (isSelected) Color(0xFF142433) else FitProSurface,
          border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) FitProPrimary else FitProSurfaceBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { selectedPlanId = plan.id }
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                  selected = isSelected,
                  onClick = { selectedPlanId = plan.id },
                  colors = RadioButtonDefaults.colors(selectedColor = FitProPrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                  Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                      color = FitProTextPrimary,
                      fontWeight = FontWeight.Bold
                    )
                  )
                  Text(
                    text = plan.billingCycle,
                    color = FitProTextMuted,
                    fontSize = 11.sp
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                  Text(
                    text = "₹${plan.pricePerMonth}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                      color = FitProPrimary,
                      fontWeight = FontWeight.ExtraBold
                    )
                  )
                  Text(
                    text = "/mo",
                    color = FitProTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                  )
                }
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = FitProPrimaryContainer
                ) {
                  Text(
                    text = "Save ${plan.discountPercent}%",
                    color = FitProPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = FitProDivider)
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              plan.features.forEach { feat ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = FitProPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = feat,
                    color = FitProTextSecondary,
                    fontSize = 12.sp
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
