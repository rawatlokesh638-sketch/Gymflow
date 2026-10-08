package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.PaymentMethod
import com.example.ui.components.FitProTopBar
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel

@Composable
fun CollectPaymentModalScreen(
  viewModel: FitProViewModel,
  onBack: () -> Unit
) {
  val member by viewModel.selectedMember.collectAsState()

  if (member == null) {
    onBack()
    return
  }
  val current = member!!

  val todayDateStr = remember {
    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
  }

  var amountText by remember {
    mutableStateOf(if (current.dueAmount > 0) current.dueAmount.toString() else current.totalFees.toString())
  }
  var paymentType by remember { mutableStateOf(PaymentMethod.UPI) }
  var paymentDate by remember { mutableStateOf(todayDateStr) }
  var notes by remember { mutableStateOf("") }

  Scaffold(
    containerColor = Color(0xFF0B0E14),
    topBar = {
      FitProTopBar(
        title = "Collect Payment",
        subtitle = null,
        showBack = true,
        onBackClick = onBack
      )
    },
    bottomBar = {
      Surface(
        color = Color(0xFF0B0E14),
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Button(
          onClick = {
            val amt = amountText.toIntOrNull() ?: 700
            viewModel.collectPayment(current.id, amt, paymentType, notes)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00C853), // Solid green matching screenshot
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("collect_payment_submit_button")
        ) {
          Text(
            text = "Collect Payment",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 16.sp
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
      contentPadding = PaddingValues(bottom = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Member Info Card matching Screenshot 8
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF131722),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
          modifier = Modifier.fillMaxWidth()
        ) {
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
                text = current.avatarInitial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = current.name,
                style = MaterialTheme.typography.titleMedium.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = current.planName,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFF8E9BAE)
                )
              )
            }
          }
        }
      }

      // 3 Stat Boxes matching Screenshot 8: Total Fees, Paid, Due (red text)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "Total Fees", color = Color(0xFF8E9BAE), fontSize = 11.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = "₹${current.totalFees}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "Paid", color = Color(0xFF8E9BAE), fontSize = 11.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = "₹${current.paidAmount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "Due", color = Color(0xFF8E9BAE), fontSize = 11.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = "₹${current.dueAmount}", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }
        }
      }

      // Payment Amount input matching Screenshot 8
      item {
        Column {
          Text(text = "Payment Amount", style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFCBD5E1)))
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
              Text(text = "₹", color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
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
            modifier = Modifier.fillMaxWidth().testTag("collect_payment_amount_input")
          )
        }
      }

      // Payment Type (Cash, UPI, Card matching Screenshot 8)
      item {
        Column {
          Text(text = "Payment Type", style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFCBD5E1)))
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            listOf(
              Pair(PaymentMethod.CASH, "Cash"),
              Pair(PaymentMethod.UPI, "UPI"),
              Pair(PaymentMethod.CARD, "Card")
            ).forEach { (mode, label) ->
              val isSelected = paymentType == mode
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF0C243B) else Color(0xFF131722),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E2433)
                ),
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { paymentType = mode }
                  .testTag("payment_type_$label")
              ) {
                Row(
                  modifier = Modifier.padding(vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = when (mode) {
                      PaymentMethod.CASH -> Icons.Default.AttachMoney
                      PaymentMethod.UPI -> Icons.Default.QrCode
                      PaymentMethod.CARD -> Icons.Default.CreditCard
                    },
                    contentDescription = label,
                    tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF8E9BAE),
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = label,
                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }

      // Payment Date
      item {
        Column {
          Text(text = "Payment Date", style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFCBD5E1)))
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = paymentDate,
            onValueChange = { paymentDate = it },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B)) },
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
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // Notes (optional)
      item {
        Column {
          Text(text = "Notes (optional)", style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFCBD5E1)))
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
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
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}
