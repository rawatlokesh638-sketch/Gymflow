package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DashboardMetrics
import com.example.data.DueMemberItem
import com.example.data.ExpiringMemberItem
import com.example.model.PaymentMethod
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
  viewModel: FitProViewModel,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var selectedPlanName by remember { mutableStateOf("General Plan") }
  var selectedPlanId by remember { mutableStateOf("plan_general") }
  var feesAmount by remember { mutableStateOf("799") }
  var paidAmount by remember { mutableStateOf("799") }
  var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }

  val plans by viewModel.plans.collectAsState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GymFlowSurface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .testTag("add_member_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Add New Member",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = GymFlowTextPrimary
            )
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = GymFlowTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Member Full Name") },
          placeholder = { Text("e.g. Rahul Sharma") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_member_name")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Mobile Number") },
          placeholder = { Text("e.g. 98123 45678") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_member_phone")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Select Plan",
          style = MaterialTheme.typography.labelMedium.copy(
            color = GymFlowTextSecondary,
            fontWeight = FontWeight.SemiBold
          )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            Triple("plan_general", "General", 799),
            Triple("plan_student", "Student", 499),
            Triple("plan_female", "Female", 699)
          ).forEach { (id, label, price) ->
            val isSelected = selectedPlanId == id
            FilterChip(
              selected = isSelected,
              onClick = {
                selectedPlanId = id
                selectedPlanName = "$label Plan"
                feesAmount = price.toString()
                paidAmount = price.toString()
              },
              label = { Text("$label (₹$price)") },
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = feesAmount,
            onValueChange = { feesAmount = it },
            label = { Text("Total Fees (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = paidAmount,
            onValueChange = { paidAmount = it },
            label = { Text("Paid Now (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Payment Mode",
          style = MaterialTheme.typography.labelMedium.copy(
            color = GymFlowTextSecondary,
            fontWeight = FontWeight.SemiBold
          )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(PaymentMethod.UPI, PaymentMethod.CASH, PaymentMethod.CARD).forEach { mode ->
            val isSelected = selectedPaymentMethod == mode
            FilterChip(
              selected = isSelected,
              onClick = { selectedPaymentMethod = mode },
              label = { Text(mode.name) },
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            val fees = feesAmount.toIntOrNull() ?: 799
            val paid = paidAmount.toIntOrNull() ?: fees
            viewModel.addMember(
              name = name,
              phone = phone,
              dob = "01 Jan 2000",
              gender = "Male",
              planId = selectedPlanId,
              planName = selectedPlanName,
              feesAmount = fees,
              paymentType = selectedPaymentMethod
            )
            onDismiss()
          },
          enabled = name.isNotBlank() && phone.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("save_member_button")
        ) {
          Text("Register Member", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      }
    }
  }
}

@Composable
fun CollectPaymentDialog(
  viewModel: FitProViewModel,
  memberId: String,
  memberName: String,
  dueAmount: Int,
  onDismiss: () -> Unit
) {
  var amountText by remember { mutableStateOf(dueAmount.toString()) }
  var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }
  var notes by remember { mutableStateOf("Reception fee recovery") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GymFlowSurface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .testTag("collect_payment_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Collect Payment",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = GymFlowTextPrimary
            )
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = GymFlowTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GymFlowWarningContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = memberName,
                fontWeight = FontWeight.Bold,
                color = GymFlowOnWarningContainer,
                fontSize = 15.sp
              )
              Text(
                text = "Outstanding Due",
                color = GymFlowOnWarningContainer.copy(alpha = 0.8f),
                fontSize = 12.sp
              )
            }
            Text(
              text = "₹$dueAmount",
              fontWeight = FontWeight.ExtraBold,
              color = GymFlowDanger,
              fontSize = 20.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text("Amount to Collect (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("collect_amount_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Payment Mode",
          style = MaterialTheme.typography.labelMedium.copy(
            color = GymFlowTextSecondary,
            fontWeight = FontWeight.SemiBold
          )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(PaymentMethod.UPI, PaymentMethod.CASH, PaymentMethod.CARD).forEach { mode ->
            val isSelected = selectedMethod == mode
            FilterChip(
              selected = isSelected,
              onClick = { selectedMethod = mode },
              label = { Text(mode.name) },
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            val amount = amountText.toIntOrNull() ?: 0
            if (amount > 0) {
              viewModel.collectPayment(memberId, amount, selectedMethod, notes)
              onDismiss()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("confirm_collect_button")
        ) {
          Text("Record ₹$amountText Received", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      }
    }
  }
}

@Composable
fun AllDueMembersDialog(
  viewModel: FitProViewModel,
  dueMembers: List<DueMemberItem>,
  onDismiss: () -> Unit,
  onCollect: (DueMemberItem) -> Unit
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }

  val filtered = dueMembers.filter {
    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GymFlowSurface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 600.dp)
        .padding(vertical = 12.dp)
        .testTag("all_due_members_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Due Members (${dueMembers.size})",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary
              )
            )
            Text(
              text = "Fee recovery backlog",
              style = MaterialTheme.typography.bodySmall.copy(color = GymFlowTextSecondary)
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = GymFlowTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by name or phone...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filtered) { item ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = GymFlowSurfaceVariant,
              border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTextPrimary,
                    fontSize = 15.sp
                  )
                  Text(
                    text = "${item.planName} • ${item.phone}",
                    color = GymFlowTextSecondary,
                    fontSize = 12.sp
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "₹${item.dueAmount} due • ${item.daysOverdue} days overdue",
                    color = GymFlowDanger,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                  )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  OutlinedButton(
                    onClick = { viewModel.sendReminder(context, item.memberId) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                  ) {
                    Text("Remind", fontSize = 12.sp, color = GymFlowPrimary)
                  }

                  Button(
                    onClick = { onCollect(item) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                  ) {
                    Text("Collect", fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AllExpiringMembersDialog(
  viewModel: FitProViewModel,
  expiringMembers: List<ExpiringMemberItem>,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GymFlowSurface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 560.dp)
        .padding(vertical = 12.dp)
        .testTag("all_expiring_members_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Expiring Soon (${expiringMembers.size})",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary
              )
            )
            Text(
              text = "Memberships expiring within 7 days",
              style = MaterialTheme.typography.bodySmall.copy(color = GymFlowTextSecondary)
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = GymFlowTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(expiringMembers) { item ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = GymFlowSurfaceVariant,
              border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTextPrimary,
                    fontSize = 15.sp
                  )
                  Text(
                    text = "${item.planName} • ${item.phone}",
                    color = GymFlowTextSecondary,
                    fontSize = 12.sp
                  )
                  Text(
                    text = item.statusText,
                    color = GymFlowWarning,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                  )
                }

                Button(
                  onClick = { viewModel.sendReminder(context, item.memberId) },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.height(38.dp)
                ) {
                  Text("Remind", fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun GymSettingsAndDataDialog(
  viewModel: FitProViewModel,
  onDismiss: () -> Unit
) {
  val settings by viewModel.settings.collectAsState()
  val isOffline by viewModel.isOffline.collectAsState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GymFlowSurface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .testTag("gym_settings_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "GymFlow Overview",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary
              )
            )
            Text(
              text = "${settings.gymName} • ${settings.gymLocation}",
              style = MaterialTheme.typography.bodySmall.copy(color = GymFlowTextSecondary)
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = GymFlowTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data testing / control options
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GymFlowSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Data Management",
              fontWeight = FontWeight.Bold,
              color = GymFlowTextPrimary,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(
              onClick = {
                viewModel.seedSampleData()
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Dataset, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Load Sample Records (186 Members)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
              onClick = {
                viewModel.resetAllDataToZero()
                onDismiss()
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = GymFlowDanger, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Reset All Data to 0 (Clean Slate)", color = GymFlowDanger)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Connectivity simulation toggle
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GymFlowSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Offline Mode",
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary,
                fontSize = 14.sp
              )
              Text(
                text = if (isOffline) "Viewing cached offline data" else "Real-time sync active",
                color = GymFlowTextSecondary,
                fontSize = 12.sp
              )
            }
            Switch(
              checked = isOffline,
              onCheckedChange = { viewModel.toggleOffline() }
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Language toggle
        OutlinedButton(
          onClick = {
            viewModel.toggleLanguage()
            onDismiss()
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Switch Language (English / हिंदी)")
        }
      }
    }
  }
}
