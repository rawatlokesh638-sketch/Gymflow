package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Member
import com.example.model.PaymentMethod
import com.example.ui.components.GymFlowTopBar
import com.example.ui.theme.*
import com.example.util.DateUtils
import com.example.viewmodel.FitProViewModel
import com.example.viewmodel.SubScreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemberScreen(
  viewModel: FitProViewModel,
  memberToEditId: String? = null,
  onBack: () -> Unit
) {
  val plans by viewModel.plans.collectAsState()
  val allMembers by viewModel.members.collectAsState()

  val existingMember = remember(memberToEditId, allMembers) {
    if (memberToEditId != null) allMembers.firstOrNull { it.id == memberToEditId } else null
  }
  val isEditMode = existingMember != null

  val activePlans = remember(plans) { plans.filter { it.active } }
  val defaultPlan = remember(activePlans) {
    activePlans.firstOrNull { it.id == "plan_general" } ?: activePlans.firstOrNull()
  }

  // Personal Information
  var name by remember(existingMember) { mutableStateOf(existingMember?.name ?: "") }
  var phone by remember(existingMember) { mutableStateOf(existingMember?.mobile ?: "") }
  var photoUrl by remember(existingMember) { mutableStateOf(existingMember?.photoUrl) }
  var dob by remember(existingMember) { mutableStateOf(existingMember?.dob ?: "15 Jan 1998") }
  var gender by remember(existingMember) { mutableStateOf(existingMember?.gender ?: "Male") }
  var address by remember(existingMember) { mutableStateOf(existingMember?.address ?: "") }
  var emergencyContact by remember(existingMember) { mutableStateOf(existingMember?.emergencyContact ?: "") }

  // Membership & Fees
  var selectedPlan by remember(existingMember, activePlans) {
    mutableStateOf(
      if (existingMember != null) activePlans.firstOrNull { it.id == existingMember.planId } ?: defaultPlan
      else defaultPlan
    )
  }
  var joinDate by remember(existingMember) {
    mutableStateOf(existingMember?.joinDate ?: DateUtils.getTodayFormatted())
  }
  var inclusiveJoinDay by remember { mutableStateOf(true) } // Day 1 config
  var isExpiryManuallyEdited by remember { mutableStateOf(isEditMode) }
  var expiryDate by remember(existingMember, selectedPlan, joinDate) {
    mutableStateOf(
      existingMember?.expiryDate ?: DateUtils.calculateExpiryDate(
        joinDateStr = joinDate,
        durationDays = selectedPlan?.durationDays ?: 30,
        inclusiveJoinDay = inclusiveJoinDay
      )
    )
  }

  var totalFeesInput by remember(existingMember, selectedPlan) {
    mutableStateOf(
      existingMember?.totalFees?.toString() ?: (selectedPlan?.price?.toString() ?: "799")
    )
  }
  var paidAmountInput by remember(existingMember, totalFeesInput) {
    mutableStateOf(
      existingMember?.paidAmount?.toString() ?: totalFeesInput
    )
  }
  var paymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }
  var notes by remember(existingMember) { mutableStateOf(existingMember?.notes ?: "") }

  // Validation States
  var nameError by remember { mutableStateOf<String?>(null) }
  var phoneError by remember { mutableStateOf<String?>(null) }
  var expiryError by remember { mutableStateOf<String?>(null) }
  var duplicateMemberFound by remember { mutableStateOf<Member?>(null) }

  // Date picker states
  var showJoinDatePicker by remember { mutableStateOf(false) }
  var showExpiryDatePicker by remember { mutableStateOf(false) }
  var showDobDatePicker by remember { mutableStateOf(false) }
  var showAvatarPicker by remember { mutableStateOf(false) }

  // Photo picker launcher
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      photoUrl = uri.toString()
      viewModel.showSnackbar("Photo selected")
    }
  }

  // Auto-update expiry when plan or joinDate changes (unless manually edited)
  LaunchedEffect(selectedPlan, joinDate, inclusiveJoinDay) {
    if (!isExpiryManuallyEdited && selectedPlan != null) {
      expiryDate = DateUtils.calculateExpiryDate(
        joinDateStr = joinDate,
        durationDays = selectedPlan!!.durationDays,
        inclusiveJoinDay = inclusiveJoinDay
      )
    }
  }

  val totalFeesNumber = totalFeesInput.toIntOrNull() ?: 0
  val paidAmountNumber = paidAmountInput.toIntOrNull() ?: 0
  val calculatedDue = maxOf(0, totalFeesNumber - paidAmountNumber)

  fun validateAndSubmit(forceDuplicate: Boolean = false) {
    nameError = null
    phoneError = null
    expiryError = null

    if (name.trim().isBlank()) {
      nameError = "Member full name is mandatory"
      return
    }

    val clean10 = DateUtils.get10DigitMobile(phone)
    if (clean10.length != 10) {
      phoneError = "Please enter a valid 10-digit Indian mobile number"
      return
    }

    // Expiry date validation
    val parsedJoin = DateUtils.parseDate(joinDate)
    val parsedExpiry = DateUtils.parseDate(expiryDate)
    if (parsedJoin != null && parsedExpiry != null && parsedExpiry.before(parsedJoin)) {
      expiryError = "Expiry date cannot be earlier than join date"
      return
    }

    // Check duplicate mobile
    if (!forceDuplicate && !isEditMode) {
      val duplicate = viewModel.checkDuplicateMobile(phone, excludeMemberId = existingMember?.id)
      if (duplicate != null) {
        duplicateMemberFound = duplicate
        return
      }
    }

    val plan = selectedPlan ?: defaultPlan ?: plans.first()
    if (isEditMode && existingMember != null) {
      val updated = existingMember.copy(
        name = name.trim(),
        mobile = DateUtils.normalizeIndianMobile(phone),
        photoUrl = photoUrl,
        dob = dob,
        gender = gender,
        address = address.trim(),
        emergencyContact = emergencyContact.trim(),
        planId = plan.id,
        planName = plan.name,
        joinDate = joinDate,
        expiryDate = expiryDate,
        totalFees = totalFeesNumber,
        paidAmount = paidAmountNumber,
        dueAmount = calculatedDue,
        notes = notes.trim()
      )
      viewModel.updateMember(updated)
    } else {
      viewModel.addMember(
        name = name.trim(),
        phone = phone,
        photoUrl = photoUrl,
        dob = dob,
        gender = gender,
        address = address.trim(),
        emergencyContact = emergencyContact.trim(),
        planId = plan.id,
        planName = plan.name,
        joinDate = joinDate,
        expiryDate = expiryDate,
        feesAmount = totalFeesNumber,
        paidAmount = paidAmountNumber,
        paymentType = paymentMethod,
        notes = notes.trim()
      )
    }
  }

  Scaffold(
    containerColor = GymFlowBackground,
    topBar = {
      GymFlowTopBar(
        title = if (isEditMode) "Edit Member Details" else "Add New Member",
        subtitle = if (isEditMode) existingMember?.name else "Enter member & membership details",
        showBack = true,
        onBackClick = onBack
      )
    },
    bottomBar = {
      Surface(
        color = GymFlowSurface,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(50.dp)
          ) {
            Text("Cancel", color = GymFlowTextSecondary, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = { validateAndSubmit(forceDuplicate = false) },
            colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(2f)
              .height(50.dp)
              .testTag("save_member_button")
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isEditMode) "Update Member" else "Save & Register",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
              )
            )
          }
        }
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Avatar / Photo section
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(GymFlowSurfaceVariant)
                .border(2.dp, GymFlowPrimary, CircleShape)
                .clickable { showAvatarPicker = true },
              contentAlignment = Alignment.Center
            ) {
              if (name.isNotBlank()) {
                val initials = name.trim().split(" ")
                  .filter { it.isNotBlank() }
                  .take(2)
                  .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                  .joinToString("")
                Text(
                  text = initials.ifBlank { "GF" },
                  color = GymFlowPrimary,
                  fontSize = 30.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = "Avatar",
                  tint = GymFlowTextSecondary,
                  modifier = Modifier.size(50.dp)
                )
              }

              // Camera action badge
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .align(Alignment.BottomEnd)
                  .clip(CircleShape)
                  .background(GymFlowPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = "Upload Photo",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              TextButton(
                onClick = {
                  photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                      ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                  )
                }
              ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = GymFlowPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gallery / Camera", fontSize = 12.sp, color = GymFlowPrimary, fontWeight = FontWeight.Bold)
              }

              if (photoUrl != null) {
                TextButton(onClick = { photoUrl = null }) {
                  Text("Remove", fontSize = 12.sp, color = GymFlowDanger)
                }
              }
            }
          }
        }
      }

      // SECTION 1: Personal Information
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = GymFlowSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Personal Information",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary,
                fontSize = 15.sp
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Full Name (Mandatory)
            OutlinedTextField(
              value = name,
              onValueChange = {
                name = it
                if (nameError != null) nameError = null
              },
              label = { Text("Full Name *") },
              placeholder = { Text("e.g. Rohit Sharma") },
              leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GymFlowPrimary) },
              isError = nameError != null,
              supportingText = { nameError?.let { Text(it, color = GymFlowDanger) } },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("add_member_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mobile Number (Mandatory)
            OutlinedTextField(
              value = phone,
              onValueChange = {
                phone = it
                if (phoneError != null) phoneError = null
              },
              label = { Text("Mobile Number *") },
              placeholder = { Text("e.g. 9876543210") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GymFlowPrimary) },
              isError = phoneError != null,
              supportingText = {
                if (phoneError != null) {
                  Text(phoneError!!, color = GymFlowDanger)
                } else {
                  Text("Indian 10-digit number. Normalizes to +91.", color = GymFlowTextMuted)
                }
              },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("add_member_phone_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Gender Segmented Selection (Optional)
            Text(
              text = "Gender",
              style = MaterialTheme.typography.labelMedium.copy(
                color = GymFlowTextSecondary,
                fontWeight = FontWeight.Medium
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf("Male", "Female", "Other").forEach { opt ->
                val isSelected = gender == opt
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) GymFlowPrimaryContainer else GymFlowSurfaceVariant,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GymFlowPrimary else GymFlowSurfaceBorder
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { gender = opt }
                    .padding(vertical = 2.dp)
                ) {
                  Text(
                    text = opt,
                    color = if (isSelected) GymFlowPrimary else GymFlowTextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date of Birth & Address (Optional)
            OutlinedTextField(
              value = dob,
              onValueChange = { dob = it },
              label = { Text("Date of Birth (Optional)") },
              leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = GymFlowTextSecondary) },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = address,
              onValueChange = { address = it },
              label = { Text("Address / Area (Optional)") },
              placeholder = { Text("e.g. Sector 6, Dharuhera") },
              leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GymFlowTextSecondary) },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // SECTION 2: Membership Plan & Dates
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = GymFlowSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Membership Plan & Duration",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary,
                fontSize = 15.sp
              )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Plan Selector Chips
            Text(
              text = "Select Plan *",
              style = MaterialTheme.typography.labelMedium.copy(color = GymFlowTextSecondary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              activePlans.forEach { plan ->
                val isSelected = selectedPlan?.id == plan.id
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) GymFlowPrimary else GymFlowSurfaceVariant,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GymFlowPrimary else GymFlowSurfaceBorder
                  ),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                      selectedPlan = plan
                      totalFeesInput = plan.price.toString()
                      paidAmountInput = plan.price.toString()
                      isExpiryManuallyEdited = false
                    }
                    .testTag("plan_select_${plan.id}")
                ) {
                  Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                      text = plan.name,
                      color = if (isSelected) Color.White else GymFlowTextPrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )
                    Text(
                      text = "₹${plan.price} • ${plan.durationDays} days",
                      color = if (isSelected) Color.White.copy(alpha = 0.9f) else GymFlowTextSecondary,
                      fontSize = 11.sp
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Join Date Picker Field
            OutlinedTextField(
              value = joinDate,
              onValueChange = {
                joinDate = it
              },
              label = { Text("Join Date *") },
              leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GymFlowPrimary) },
              trailingIcon = {
                IconButton(onClick = { joinDate = DateUtils.getTodayFormatted() }) {
                  Text("Today", fontSize = 11.sp, color = GymFlowPrimary, fontWeight = FontWeight.Bold)
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Configurable "Join day as Day 1" toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Count Joining Day as Day 1",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = GymFlowTextPrimary,
                    fontSize = 13.sp
                  )
                )
                Text(
                  text = if (inclusiveJoinDay) "Expiry is Join Date + ${selectedPlan?.durationDays ?: 30} - 1 day" else "Expiry is Join Date + ${selectedPlan?.durationDays ?: 30} days",
                  style = MaterialTheme.typography.bodySmall.copy(color = GymFlowTextMuted, fontSize = 11.sp)
                )
              }
              Switch(
                checked = inclusiveJoinDay,
                onCheckedChange = {
                  inclusiveJoinDay = it
                  isExpiryManuallyEdited = false
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = GymFlowPrimary
                )
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expiry Date Field (Auto-calculated, manually editable)
            OutlinedTextField(
              value = expiryDate,
              onValueChange = {
                expiryDate = it
                isExpiryManuallyEdited = true
                if (expiryError != null) expiryError = null
              },
              label = { Text("Expiry Date *") },
              leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = GymFlowWarning) },
              trailingIcon = {
                if (isExpiryManuallyEdited) {
                  IconButton(onClick = {
                    isExpiryManuallyEdited = false
                    selectedPlan?.let {
                      expiryDate = DateUtils.calculateExpiryDate(joinDate, it.durationDays, inclusiveJoinDay)
                    }
                  }) {
                    Icon(Icons.Default.Restore, contentDescription = "Auto-calc", tint = GymFlowPrimary)
                  }
                }
              },
              isError = expiryError != null,
              supportingText = {
                if (expiryError != null) {
                  Text(expiryError!!, color = GymFlowDanger)
                } else if (isExpiryManuallyEdited) {
                  Text("Manually adjusted date preserved", color = GymFlowWarning)
                } else {
                  Text("Auto-calculated from plan duration", color = GymFlowTextMuted)
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // SECTION 3: Fees & Payment
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = GymFlowSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Fees & Payment Collection",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = GymFlowTextPrimary,
                fontSize = 15.sp
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              // Total Fees
              OutlinedTextField(
                value = totalFeesInput,
                onValueChange = {
                  totalFeesInput = it.filter { ch -> ch.isDigit() }
                },
                label = { Text("Total Fees (₹) *") },
                prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = GymFlowTextPrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("add_member_fees_input")
              )

              // Paid Amount
              OutlinedTextField(
                value = paidAmountInput,
                onValueChange = {
                  paidAmountInput = it.filter { ch -> ch.isDigit() }
                },
                label = { Text("Paid Amount (₹)") },
                prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = GymFlowTextPrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("add_member_paid_input")
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick payment quick-fill buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = { paidAmountInput = totalFeesInput },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Full Paid (₹$totalFeesNumber)", fontSize = 11.sp, color = GymFlowPrimary)
              }

              OutlinedButton(
                onClick = { paidAmountInput = "0" },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Unpaid (₹0)", fontSize = 11.sp, color = GymFlowDanger)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calculated Due Banner
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (calculatedDue > 0) GymFlowDangerContainer else GymFlowSuccessContainer,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (calculatedDue > 0) GymFlowDanger.copy(alpha = 0.5f) else GymFlowSuccess.copy(alpha = 0.5f)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (calculatedDue > 0) "Remaining Due Amount:" else "Payment Status:",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (calculatedDue > 0) GymFlowOnDangerContainer else GymFlowOnSuccessContainer
                  )
                )

                Text(
                  text = if (calculatedDue > 0) "₹$calculatedDue Pending" else "✓ Paid in Full",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (calculatedDue > 0) GymFlowDanger else GymFlowSuccess
                  )
                )
              }
            }

            if (paidAmountNumber > 0) {
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Payment Mode",
                style = MaterialTheme.typography.labelMedium.copy(color = GymFlowTextSecondary)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf(PaymentMethod.UPI, PaymentMethod.CASH, PaymentMethod.CARD).forEach { method ->
                  val isSelected = paymentMethod == method
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) GymFlowPrimaryContainer else GymFlowSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                      1.dp,
                      if (isSelected) GymFlowPrimary else GymFlowSurfaceBorder
                    ),
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .clickable { paymentMethod = method }
                  ) {
                    Text(
                      text = method.name,
                      color = if (isSelected) GymFlowPrimary else GymFlowTextPrimary,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 12.sp,
                      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                      modifier = Modifier.padding(vertical = 10.dp)
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

  // Duplicate Mobile Dialog
  if (duplicateMemberFound != null) {
    val dup = duplicateMemberFound!!
    AlertDialog(
      onDismissRequest = { duplicateMemberFound = null },
      containerColor = GymFlowSurface,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = GymFlowWarning)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Mobile Already Exists", color = GymFlowTextPrimary, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(
            text = "Member with mobile number '${phone}' is already registered as:",
            color = GymFlowTextSecondary,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = GymFlowSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(text = dup.name, fontWeight = FontWeight.Bold, color = GymFlowTextPrimary)
              Text(text = "Plan: ${dup.planName} • Due: ₹${dup.dueAmount}", fontSize = 12.sp, color = GymFlowTextSecondary)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val dupId = dup.id
            duplicateMemberFound = null
            viewModel.openSubScreen(SubScreen.MEMBER_PROFILE, dupId)
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary)
        ) {
          Text("View Existing Member", color = Color.White)
        }
      },
      dismissButton = {
        Row {
          TextButton(onClick = { duplicateMemberFound = null }) {
            Text("Cancel", color = GymFlowTextSecondary)
          }
          TextButton(
            onClick = {
              duplicateMemberFound = null
              validateAndSubmit(forceDuplicate = true)
            }
          ) {
            Text("Continue Anyway", color = GymFlowWarning, fontWeight = FontWeight.Bold)
          }
        }
      }
    )
  }
}
