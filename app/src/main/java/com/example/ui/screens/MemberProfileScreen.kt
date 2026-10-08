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
import androidx.compose.material.icons.outlined.*
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
import com.example.model.MemberStatus
import com.example.model.PaymentMethod
import com.example.ui.components.GymFlowTopBar
import com.example.ui.components.MemberStatusBadge
import com.example.ui.theme.*
import com.example.util.DateUtils
import com.example.viewmodel.FitProViewModel
import com.example.viewmodel.SubScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberProfileScreen(
  viewModel: FitProViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val member by viewModel.selectedMember.collectAsState()
  val payments by viewModel.payments.collectAsState()
  val attendance by viewModel.attendance.collectAsState()
  val plans by viewModel.plans.collectAsState()

  var selectedTab by remember { mutableStateOf("Overview") }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showDeactivateDialog by remember { mutableStateOf(false) }
  var showCollectPaymentModal by remember { mutableStateOf(false) }
  var showChangePlanModal by remember { mutableStateOf(false) }

  if (member == null) {
    Scaffold(
      containerColor = GymFlowBackground,
      topBar = {
        GymFlowTopBar(title = "Member Details", showBack = true, onBackClick = onBack)
      }
    ) { p ->
      Box(modifier = Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
        Text("No member selected", color = GymFlowTextSecondary)
      }
    }
    return
  }

  val current = member!!
  val memberPayments = remember(payments, current.id) {
    payments.filter { it.memberId == current.id }
  }
  val memberAttendance = remember(attendance, current.id) {
    attendance.filter { it.memberId == current.id }
  }

  val daysRemaining = remember(current.expiryDate) {
    DateUtils.getDaysRemaining(current.expiryDate)
  }

  Scaffold(
    containerColor = GymFlowBackground,
    topBar = {
      GymFlowTopBar(
        title = "Member Profile",
        subtitle = current.displayCode,
        showBack = true,
        onBackClick = onBack,
        actions = {
          // Edit Member
          IconButton(
            onClick = { viewModel.openSubScreen(SubScreen.EDIT_MEMBER, current.id) },
            modifier = Modifier.testTag("profile_edit_button")
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Member", tint = GymFlowPrimary)
          }

          // More Options / Delete Menu
          IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.testTag("profile_delete_button")
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = GymFlowDanger)
          }
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. HERO PROFILE CARD
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = GymFlowSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Large Avatar Circle
            Box(
              modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                  when (current.status) {
                    MemberStatus.DUE -> GymFlowDangerContainer
                    MemberStatus.EXPIRING -> GymFlowWarningContainer
                    MemberStatus.EXPIRED -> Color(0xFFFFEBEE)
                    else -> GymFlowPrimaryContainer
                  }
                )
                .border(
                  2.5.dp,
                  when (current.status) {
                    MemberStatus.DUE -> GymFlowDanger
                    MemberStatus.EXPIRING -> GymFlowWarning
                    MemberStatus.EXPIRED -> Color(0xFFC62828)
                    else -> GymFlowPrimary
                  },
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = current.avatarInitial,
                color = when (current.status) {
                  MemberStatus.DUE -> GymFlowOnDangerContainer
                  MemberStatus.EXPIRING -> GymFlowOnWarningContainer
                  MemberStatus.EXPIRED -> Color(0xFFC62828)
                  else -> GymFlowPrimary
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = current.name,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary,
                fontSize = 20.sp
              )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = "📱 ${current.mobile}",
              style = MaterialTheme.typography.bodyMedium.copy(
                color = GymFlowTextSecondary,
                fontSize = 14.sp
              )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              MemberStatusBadge(status = current.status)
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GymFlowSurfaceVariant
              ) {
                Text(
                  text = current.planName,
                  color = GymFlowTextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Communication Bar: Call & WhatsApp buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = { viewModel.dialMember(context, current.mobile) },
                colors = ButtonDefaults.buttonColors(containerColor = GymFlowSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
              ) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = GymFlowPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call", color = GymFlowTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }

              Button(
                onClick = { viewModel.sendWhatsApp(context, current) },
                colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
              ) {
                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }

      // 2. FINANCIAL OVERVIEW CARDS (Fees, Paid, Due)
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = GymFlowSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Fee & Balance Status",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary,
                fontSize = 15.sp
              )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Total Fees
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = GymFlowSurfaceVariant,
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("Total Fees", fontSize = 11.sp, color = GymFlowTextSecondary)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text("₹${current.totalFees}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = GymFlowTextPrimary)
                }
              }

              // Paid Amount
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = GymFlowSuccessContainer,
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("Paid", fontSize = 11.sp, color = GymFlowOnSuccessContainer)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text("₹${current.paidAmount}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = GymFlowSuccess)
                }
              }

              // Due Amount
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (current.dueAmount > 0) GymFlowDangerContainer else GymFlowSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (current.dueAmount > 0) GymFlowDanger else GymFlowSurfaceBorder
                ),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(
                    "Due Amount",
                    fontSize = 11.sp,
                    color = if (current.dueAmount > 0) GymFlowOnDangerContainer else GymFlowTextSecondary
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    "₹${current.dueAmount}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (current.dueAmount > 0) GymFlowDanger else GymFlowSuccess
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row for fees: Collect Payment / Mark Paid button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (current.dueAmount > 0) {
                Button(
                  onClick = { showCollectPaymentModal = true },
                  colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                ) {
                  Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Collect Due ₹${current.dueAmount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }

              OutlinedButton(
                onClick = { showChangePlanModal = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
              ) {
                Icon(Icons.Default.SyncAlt, contentDescription = null, tint = GymFlowPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Change Plan", color = GymFlowPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }

      // 3. MEMBERSHIP VALIDITY & TIMELINE
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = GymFlowSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Membership Validity",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary,
                fontSize = 15.sp
              )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Joined On", fontSize = 11.sp, color = GymFlowTextSecondary)
                Text(current.joinDate, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GymFlowTextPrimary)
              }

              Icon(Icons.Default.ArrowForward, contentDescription = null, tint = GymFlowTextMuted)

              Column(horizontalAlignment = Alignment.End) {
                Text("Expires On", fontSize = 11.sp, color = GymFlowTextSecondary)
                Text(
                  current.expiryDate,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (DateUtils.isExpired(current.expiryDate)) GymFlowDanger else GymFlowTextPrimary
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Days status chip
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = when {
                DateUtils.isExpired(current.expiryDate) -> Color(0xFFFFEBEE)
                DateUtils.isExpiringWithin7Days(current.expiryDate) -> GymFlowWarningContainer
                else -> GymFlowPrimaryContainer
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = when {
                  DateUtils.isExpired(current.expiryDate) -> "Expired ${-daysRemaining} days ago"
                  daysRemaining == 0 -> "Expires today!"
                  daysRemaining <= 7 -> "Expiring soon: $daysRemaining days left"
                  else -> "$daysRemaining days of active membership remaining"
                },
                color = when {
                  DateUtils.isExpired(current.expiryDate) -> Color(0xFFC62828)
                  DateUtils.isExpiringWithin7Days(current.expiryDate) -> GymFlowOnWarningContainer
                  else -> GymFlowPrimary
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // 4. TAB CONTROLS (Details, Payment History, Attendance)
      item {
        TabRow(
          selectedTabIndex = when (selectedTab) {
            "History" -> 1
            "Attendance" -> 2
            else -> 0
          },
          containerColor = GymFlowSurface,
          contentColor = GymFlowPrimary
        ) {
          Tab(
            selected = selectedTab == "Overview",
            onClick = { selectedTab = "Overview" },
            text = { Text("Details", fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == "History",
            onClick = { selectedTab = "History" },
            text = { Text("Payments (${memberPayments.size})", fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == "Attendance",
            onClick = { selectedTab = "Attendance" },
            text = { Text("Attendance (${memberAttendance.size})", fontWeight = FontWeight.Bold) }
          )
        }
      }

      // TAB 1: Member Personal Details
      if (selectedTab == "Overview") {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GymFlowSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              DetailRow(label = "Member ID", value = current.displayCode)
              DetailRow(label = "Date of Birth", value = current.dob.ifBlank { "Not specified" })
              DetailRow(label = "Gender", value = current.gender)
              DetailRow(label = "Address", value = current.address.ifBlank { "Dharuhera, Haryana" })
              DetailRow(label = "Emergency Contact", value = current.emergencyContact.ifBlank { "Not provided" })
              DetailRow(label = "Notes", value = current.notes.ifBlank { "No special notes" })
            }
          }
        }

        // Deactivate Member Button
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (current.status == MemberStatus.INACTIVE) {
              Button(
                onClick = { viewModel.reactivateMember(current.id) },
                colors = ButtonDefaults.buttonColors(containerColor = GymFlowSuccess),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reactivate Member", color = Color.White, fontWeight = FontWeight.Bold)
              }
            } else {
              OutlinedButton(
                onClick = { showDeactivateDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GymFlowTextSecondary),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
              ) {
                Icon(Icons.Default.Block, contentDescription = null, tint = GymFlowTextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Deactivate Member", fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      // TAB 2: Payment History
      if (selectedTab == "History") {
        if (memberPayments.isEmpty()) {
          item {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = GymFlowSurface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "No payments recorded yet for this member.",
                color = GymFlowTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(20.dp)
              )
            }
          }
        } else {
          items(memberPayments, key = { it.id }) { pay ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = GymFlowSurface,
              border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
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
                    text = "₹${pay.amount}",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = GymFlowSuccess,
                      fontSize = 16.sp
                    )
                  )
                  Text(
                    text = "${pay.paymentDate} • ${pay.paymentMethod.name}",
                    fontSize = 12.sp,
                    color = GymFlowTextSecondary
                  )
                }

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = GymFlowSurfaceVariant
                ) {
                  Text(
                    text = pay.invoiceNo.ifBlank { "RECEIPT" },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
            }
          }
        }
      }

      // TAB 3: Attendance Summary
      if (selectedTab == "Attendance") {
        if (memberAttendance.isEmpty()) {
          item {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = GymFlowSurface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "No attendance check-ins recorded yet.",
                color = GymFlowTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(20.dp)
              )
            }
          }
        } else {
          items(memberAttendance, key = { it.id }) { att ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = GymFlowSurface,
              border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
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
                    text = att.date,
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTextPrimary,
                    fontSize = 14.sp
                  )
                  Text(
                    text = "${att.checkInTime} • ${att.method.name}",
                    fontSize = 12.sp,
                    color = GymFlowTextSecondary
                  )
                }

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = GymFlowSuccessContainer
                ) {
                  Text(
                    text = "Present",
                    color = GymFlowOnSuccessContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // DIALOG: Delete Confirmation
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      containerColor = GymFlowSurface,
      title = { Text("Delete Member?", color = GymFlowTextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Text(
          text = "Are you sure you want to remove ${current.name} from gym records? This cannot be undone.",
          color = GymFlowTextSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteDialog = false
            viewModel.deleteMember(current.id)
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymFlowDanger)
        ) {
          Text("Delete Record", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel", color = GymFlowTextSecondary)
        }
      }
    )
  }

  // DIALOG: Deactivate Confirmation
  if (showDeactivateDialog) {
    AlertDialog(
      onDismissRequest = { showDeactivateDialog = false },
      containerColor = GymFlowSurface,
      title = { Text("Deactivate Member?", color = GymFlowTextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Text(
          text = "Deactivating will pause ${current.name}'s membership without deleting past fees or attendance.",
          color = GymFlowTextSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showDeactivateDialog = false
            viewModel.deactivateMember(current.id)
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary)
        ) {
          Text("Deactivate", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeactivateDialog = false }) {
          Text("Cancel", color = GymFlowTextSecondary)
        }
      }
    )
  }

  // MODAL: Quick Collect Payment
  if (showCollectPaymentModal) {
    var collectAmountInput by remember { mutableStateOf(current.dueAmount.toString()) }
    var collectMethod by remember { mutableStateOf(PaymentMethod.UPI) }

    AlertDialog(
      onDismissRequest = { showCollectPaymentModal = false },
      containerColor = GymFlowSurface,
      title = { Text("Collect Due Payment", color = GymFlowTextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Member: ${current.name}", fontWeight = FontWeight.Medium, color = GymFlowTextSecondary)

          OutlinedTextField(
            value = collectAmountInput,
            onValueChange = { collectAmountInput = it.filter { ch -> ch.isDigit() } },
            label = { Text("Amount (₹)") },
            prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )

          Text("Payment Mode", fontSize = 12.sp, color = GymFlowTextSecondary)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(PaymentMethod.UPI, PaymentMethod.CASH, PaymentMethod.CARD).forEach { m ->
              val isSel = collectMethod == m
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSel) GymFlowPrimaryContainer else GymFlowSurfaceVariant,
                modifier = Modifier
                  .weight(1f)
                  .clickable { collectMethod = m }
                  .padding(vertical = 2.dp)
              ) {
                Text(
                  text = m.name,
                  color = if (isSel) GymFlowPrimary else GymFlowTextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                  modifier = Modifier.padding(vertical = 8.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amt = collectAmountInput.toIntOrNull() ?: 0
            if (amt > 0) {
              viewModel.collectPayment(current.id, amt, collectMethod, "Reception collection")
              showCollectPaymentModal = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary)
        ) {
          Text("Record Payment", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCollectPaymentModal = false }) {
          Text("Cancel", color = GymFlowTextSecondary)
        }
      }
    )
  }

  // MODAL: Change Membership Plan
  if (showChangePlanModal) {
    val activePlans = plans.filter { it.active }
    var planToSelect by remember { mutableStateOf(activePlans.firstOrNull { it.id == current.planId } ?: activePlans.first()) }

    AlertDialog(
      onDismissRequest = { showChangePlanModal = false },
      containerColor = GymFlowSurface,
      title = { Text("Change Membership Plan", color = GymFlowTextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Select new plan for ${current.name}:", color = GymFlowTextSecondary, fontSize = 13.sp)

          activePlans.forEach { p ->
            val isSelected = planToSelect.id == p.id
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) GymFlowPrimaryContainer else GymFlowSurfaceVariant,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) GymFlowPrimary else GymFlowSurfaceBorder
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { planToSelect = p }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(p.name, fontWeight = FontWeight.Bold, color = GymFlowTextPrimary)
                  Text("${p.durationDays} days validity", fontSize = 11.sp, color = GymFlowTextSecondary)
                }
                Text("₹${p.price}", fontWeight = FontWeight.Bold, color = GymFlowPrimary)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val newExpiry = DateUtils.calculateExpiryDate(current.joinDate, planToSelect.durationDays)
            viewModel.changeMemberPlan(
              memberId = current.id,
              planId = planToSelect.id,
              planName = planToSelect.name,
              newExpiryDate = newExpiry,
              newFees = planToSelect.price
            )
            showChangePlanModal = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary)
        ) {
          Text("Confirm Plan Change", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showChangePlanModal = false }) {
          Text("Cancel", color = GymFlowTextSecondary)
        }
      }
    )
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, color = GymFlowTextSecondary, fontSize = 13.sp)
    Text(text = value, color = GymFlowTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
  }
}
