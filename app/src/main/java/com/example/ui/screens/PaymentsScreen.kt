package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentMethod
import com.example.ui.components.FitProTopBar
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel
import com.example.viewmodel.SubScreen

@Composable
fun PaymentsScreen(
  viewModel: FitProViewModel
) {
  val payments by viewModel.payments.collectAsState()
  val members by viewModel.members.collectAsState()

  var viewMode by remember { mutableStateOf("PAYMENT_HISTORY") } // "PAYMENT_HISTORY" (Screen 9) vs "DUE_MEMBERS" (Screen 10)
  var dueFilter by remember { mutableStateOf("ALL") } // "ALL", "1_7_DAYS", "7_PLUS_DAYS"
  var dueSearch by remember { mutableStateOf("") }

  val dueMembers = members.filter { it.dueAmount > 0 }
  val targetMember = members.firstOrNull()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0B0E14))
      .testTag("payments_screen"),
    contentPadding = PaddingValues(bottom = 120.dp)
  ) {
    if (viewMode == "PAYMENT_HISTORY") {
      // SCREEN 9: PAYMENT HISTORY
      item {
        FitProTopBar(
          title = "Payment History",
          subtitle = null,
          actions = {
            IconButton(onClick = { viewMode = "DUE_MEMBERS" }) {
              Icon(Icons.Default.FilterList, contentDescription = "Due Members", tint = Color(0xFF00C853))
            }
          }
        )
      }

      // Member Info Card matching Screenshot 9
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF131722),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          if (targetMember != null) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF263238)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = targetMember.avatarInitial,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column {
                Text(
                  text = targetMember.name,
                  style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                  )
                )
                Text(
                  text = targetMember.phone,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF8E9BAE)
                  )
                )
              }
            }
          } else {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF263238)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF8E9BAE))
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text("Total Collections: ₹0", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Add members to record transactions", color = Color(0xFF8E9BAE), fontSize = 12.sp)
              }
            }
          }
        }
      }

      // Tab switcher
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF00C853),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(20.dp))
              .clickable { viewMode = "PAYMENT_HISTORY" }
          ) {
            Text(
              text = "Payment History (${payments.size})",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(20.dp))
              .clickable { viewMode = "DUE_MEMBERS" }
          ) {
            Text(
              text = "Due Members (${dueMembers.size})",
              color = Color(0xFF8E9BAE),
              fontWeight = FontWeight.Medium,
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          }
        }
      }

      // Transaction list or empty state
      if (payments.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 16.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text("No payments recorded yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text("Collected fees and subscription receipts will appear here.", color = Color(0xFF8E9BAE), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
          }
        }
      } else {
        items(payments) { pay ->
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                      when (pay.paymentType) {
                        PaymentMethod.CASH -> Color(0xFF0E3823)
                        PaymentMethod.UPI -> Color(0xFF0F324D)
                        PaymentMethod.CARD -> Color(0xFF3B2609)
                      }
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = when (pay.paymentType) {
                      PaymentMethod.CASH -> Icons.Default.AttachMoney
                      PaymentMethod.UPI -> Icons.Default.QrCode
                      PaymentMethod.CARD -> Icons.Default.CreditCard
                    },
                    contentDescription = null,
                    tint = when (pay.paymentType) {
                      PaymentMethod.CASH -> Color(0xFF00E676)
                      PaymentMethod.UPI -> Color(0xFF00E5FF)
                      PaymentMethod.CARD -> Color(0xFFFFB300)
                    },
                    modifier = Modifier.size(18.dp)
                  )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                  Text(
                    text = "₹${pay.amount}",
                    style = MaterialTheme.typography.titleMedium.copy(
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp
                    )
                  )
                  Text(
                    text = "${pay.memberName} • ${pay.paymentType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = Color(0xFF8E9BAE),
                      fontSize = 11.sp
                    )
                  )
                }
              }

              Text(
                text = pay.date,
                color = Color(0xFF8E9BAE),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    } else {
      // SCREEN 10: DUE MEMBERS
      item {
        FitProTopBar(
          title = "Due Members (${dueMembers.size})",
          subtitle = null,
          showBack = true,
          onBackClick = { viewMode = "PAYMENT_HISTORY" },
          actions = {
            IconButton(onClick = { viewModel.showSnackbar("Due Filter") }) {
              Icon(Icons.Default.Tune, contentDescription = "Filter", tint = Color(0xFFCBD5E1))
            }
          }
        )
      }

      // Search bar
      item {
        OutlinedTextField(
          value = dueSearch,
          onValueChange = { dueSearch = it },
          placeholder = { Text("Search by name or mobile...", color = Color(0xFF64748B), fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00C853),
            unfocusedBorderColor = Color(0xFF1E2433),
            focusedContainerColor = Color(0xFF131722),
            unfocusedContainerColor = Color(0xFF131722),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        )
      }

      // Filter chips: All, 1-7 Days, 7+ Days
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val due1to7Count = dueMembers.count { it.daysOverdue in 1..7 }
          val due7PlusCount = dueMembers.count { it.daysOverdue > 7 }
          listOf(
            Pair("ALL", "All (${dueMembers.size})"),
            Pair("1_7_DAYS", "1-7 Days ($due1to7Count)"),
            Pair("7_PLUS_DAYS", "7+ Days ($due7PlusCount)")
          ).forEach { (key, label) ->
            val isSelected = dueFilter == key
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) Color(0xFF00C853) else Color(0xFF131722),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) Color(0xFF00C853) else Color(0xFF1E2433)
              ),
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { dueFilter = key }
            ) {
              Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF8E9BAE),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      val dueListFiltered = dueMembers.filter { m ->
        val matchesQuery = dueSearch.isBlank() || m.name.contains(dueSearch, ignoreCase = true)
        val matchesDays = when (dueFilter) {
          "1_7_DAYS" -> m.daysOverdue in 1..7
          "7_PLUS_DAYS" -> m.daysOverdue > 7
          else -> true
        }
        matchesQuery && matchesDays
      }

      if (dueListFiltered.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 20.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(40.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text("No pending dues!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text("All member accounts are completely cleared.", color = Color(0xFF8E9BAE), fontSize = 12.sp)
            }
          }
        }
      } else {
        items(dueListFiltered) { m ->
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
              .clickable { viewModel.openSubScreen(SubScreen.MEMBER_PROFILE, m.id) }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF263238)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = m.avatarInitial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = m.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                  )
                  Text(
                    text = "${m.planName} • Exp: ${m.expiryDate}",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = Color(0xFF8E9BAE),
                      fontSize = 11.sp
                    )
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "₹${m.dueAmount}",
                  style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                )
                Text(
                  text = "${m.daysOverdue} days",
                  color = Color(0xFFFF5252),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        // Bottom Action Buttons
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                if (dueMembers.isNotEmpty()) {
                  viewModel.openSubScreen(SubScreen.WHATSAPP_REMINDER)
                } else {
                  viewModel.showSnackbar("No due members. All payments clear!")
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1.3f)
                .height(48.dp)
            ) {
              Text("+ Send Reminder", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            OutlinedButton(
              onClick = {
                if (dueMembers.isNotEmpty()) {
                  viewModel.showSnackbar("Exported due list (${dueMembers.size} members)")
                } else {
                  viewModel.showSnackbar("No due members to export. All accounts clear!")
                }
              },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
            ) {
              Text("Export List", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
          }
        }
      }
    }
  }
}
