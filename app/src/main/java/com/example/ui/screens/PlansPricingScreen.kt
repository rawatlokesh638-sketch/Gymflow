package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MembershipPlan
import com.example.ui.components.FitProTopBar
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel

@Composable
fun PlansPricingScreen(
  viewModel: FitProViewModel,
  onBack: () -> Unit
) {
  val plans by viewModel.plans.collectAsState()
  val language by viewModel.language.collectAsState()

  var editingPlan by remember { mutableStateOf<MembershipPlan?>(null) }
  var editedPrice by remember { mutableStateOf("") }

  if (editingPlan != null) {
    val plan = editingPlan!!
    AlertDialog(
      onDismissRequest = { editingPlan = null },
      containerColor = FitProSurface,
      title = {
        Text(
          text = if (language == "hi") "${plan.name} की कीमत बदलें" else "Adjust Fee for ${plan.name}",
          color = FitProTextPrimary,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column {
          Text(
            text = if (language == "hi") "क्लाइंट के बजट अनुसार किफायती कीमत दर्ज करें:" else "Set an affordable price that your local gym clients can comfortably pay:",
            color = FitProTextSecondary,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = editedPrice,
            onValueChange = { editedPrice = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
              Text("₹", color = FitProPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = FitProPrimary,
              unfocusedBorderColor = FitProSurfaceBorder,
              focusedContainerColor = FitProSurfaceVariant,
              unfocusedContainerColor = FitProSurfaceVariant,
              focusedTextColor = FitProTextPrimary,
              unfocusedTextColor = FitProTextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val newP = editedPrice.toIntOrNull() ?: plan.price
            viewModel.updatePlanPrice(plan.id, newP)
            editingPlan = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = FitProPrimary,
            contentColor = FitProOnPrimary
          )
        ) {
          Text("Save Rate", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { editingPlan = null }) {
          Text("Cancel", color = FitProTextSecondary)
        }
      }
    )
  }

  Scaffold(
    containerColor = FitProBackground,
    topBar = {
      FitProTopBar(
        title = if (language == "hi") "जिम प्लान्स और दरें" else "Plans & Client Pricing",
        subtitle = "Affordable Gym Pricing Control",
        showBack = true,
        onBackClick = onBack
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(bottom = 40.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Client affordability tip banner
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF0B2418),
          border = androidx.compose.foundation.BorderStroke(1.dp, FitProPrimary.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Savings,
              contentDescription = null,
              tint = FitProPrimary,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (language == "hi") "किफायती फीस = अधिक मेंबर्स!" else "Pocket-Friendly Fees = More Retention!",
                style = MaterialTheme.typography.titleSmall.copy(
                  color = FitProTextPrimary,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = if (language == "hi") "कम और उचित फीस रखने से मेंबर्स समय पर भुगतान करते हैं और जिम नहीं छोड़ते।" else "Keeping pricing affordable ensures gym members pay on time without hesitation.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = FitProTextSecondary,
                  fontSize = 11.sp
                )
              )
            }
          }
        }
      }

      // Plans list
      items(plans) { plan ->
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = FitProSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, FitProSurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
              editingPlan = plan
              editedPrice = plan.price.toString()
            }
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                      color = FitProTextPrimary,
                      fontWeight = FontWeight.Bold
                    )
                  )
                  plan.badge?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = FitProPrimaryContainer
                    ) {
                      Text(
                        text = it,
                        color = FitProPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
                Text(
                  text = "${plan.durationMonths} Month Duration",
                  color = FitProTextMuted,
                  fontSize = 11.sp
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "₹${plan.price}",
                  style = MaterialTheme.typography.headlineSmall.copy(
                    color = FitProPrimary,
                    fontWeight = FontWeight.ExtraBold
                  )
                )
                Text(
                  text = "Tap to edit fee",
                  color = FitProCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = plan.description,
              style = MaterialTheme.typography.bodySmall.copy(
                color = FitProTextSecondary,
                lineHeight = 16.sp
              )
            )
          }
        }
      }
    }
  }
}
