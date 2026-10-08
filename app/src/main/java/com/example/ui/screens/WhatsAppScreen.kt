package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TemplateCategory
import com.example.ui.components.FitProTopBar
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppScreen(
  viewModel: FitProViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val members by viewModel.members.collectAsState()
  val templates by viewModel.templates.collectAsState()
  val settings by viewModel.settings.collectAsState()

  var selectedCategory by remember { mutableStateOf(TemplateCategory.DUE_REMINDER) }
  var selectedRecipientGroup by remember { mutableStateOf("ALL_DUE") } // "ALL_DUE", "DUE_3", "DUE_7", "CUSTOM"
  var selectedTemplate by remember { mutableStateOf(templates.firstOrNull()) }
  var templateDropdownOpen by remember { mutableStateOf(false) }

  val dueMembers = members.filter { it.dueAmount > 0 }
  val targetMember = dueMembers.firstOrNull() ?: members.firstOrNull()

  val due3Count = dueMembers.count { it.daysOverdue in 1..3 }
  val due7Count = dueMembers.count { it.daysOverdue in 1..7 }

  val previewText = "नमस्ते {name} जी, आपकी जिम की फीस ₹{amount} अभी बाकी है। कृपया जल्द से जल्द पेमेंट करें ताकि आपकी मेंबरशिप जारी रहे। धन्यवाद 🙏\n- {gym_name}"

  Scaffold(
    containerColor = Color(0xFF0B0E14),
    topBar = {
      FitProTopBar(
        title = "Send WhatsApp Message",
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
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(
            onClick = {
              if (targetMember != null) {
                val formatted = previewText
                  .replace("{name}", targetMember.name)
                  .replace("{amount}", targetMember.dueAmount.toString())
                  .replace("{gym_name}", settings.gymName)
                viewModel.sendWhatsApp(context, targetMember, formatted)
              } else {
                viewModel.showSnackbar("No members to message. Please add a member first!")
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF00C853), // Solid green matching screenshot 11
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("send_on_whatsapp_button")
          ) {
            Icon(
              imageVector = Icons.Default.Chat,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Send on WhatsApp",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
              )
            )
          }

          OutlinedButton(
            onClick = {
              if (targetMember != null) {
                val formatted = previewText
                  .replace("{name}", targetMember.name)
                  .replace("{amount}", targetMember.dueAmount.toString())
                  .replace("{gym_name}", settings.gymName)
                viewModel.sendSMS(context, targetMember, formatted)
              } else {
                viewModel.showSnackbar("No members to message. Please add a member first!")
              }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("send_sms_button")
          ) {
            Text(
              text = "Send SMS",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
      contentPadding = PaddingValues(bottom = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Tabs at top matching Screenshot 11: [Due Reminder] [Expiry] [Payment] [Birthday]
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            Pair(TemplateCategory.DUE_REMINDER, "Due Reminder"),
            Pair(TemplateCategory.EXPIRY, "Expiry"),
            Pair(TemplateCategory.PAYMENT, "Payment"),
            Pair(TemplateCategory.BIRTHDAY, "Birthday")
          ).forEach { (cat, label) ->
            val isSelected = selectedCategory == cat
            Surface(
              shape = RoundedCornerShape(18.dp),
              color = if (isSelected) Color(0xFF00C853) else Color(0xFF131722),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) Color(0xFF00C853) else Color(0xFF1E2433)
              ),
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable { selectedCategory = cat }
            ) {
              Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF8E9BAE),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            }
          }
        }
      }

      // "Select Members" checklist matching Screenshot 11
      item {
        Column {
          Text(
            text = "Select Members",
            style = MaterialTheme.typography.titleSmall.copy(
              color = Color(0xFFCBD5E1),
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.height(8.dp))

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              MemberCheckRow(
                title = "All Due Members (${dueMembers.size})",
                checked = selectedRecipientGroup == "ALL_DUE",
                onToggle = { selectedRecipientGroup = "ALL_DUE" }
              )
              HorizontalDivider(color = Color(0xFF1E2433))
              MemberCheckRow(
                title = "Due in 3 days ($due3Count)",
                checked = selectedRecipientGroup == "DUE_3",
                onToggle = { selectedRecipientGroup = "DUE_3" }
              )
              HorizontalDivider(color = Color(0xFF1E2433))
              MemberCheckRow(
                title = "Due in 7 days ($due7Count)",
                checked = selectedRecipientGroup == "DUE_7",
                onToggle = { selectedRecipientGroup = "DUE_7" }
              )
              HorizontalDivider(color = Color(0xFF1E2433))
              MemberCheckRow(
                title = "Custom Selection",
                checked = selectedRecipientGroup == "CUSTOM",
                onToggle = { selectedRecipientGroup = "CUSTOM" }
              )
            }
          }
        }
      }

      // "Message Template" dropdown matching Screenshot 11
      item {
        Column {
          Text(
            text = "Message Template",
            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFCBD5E1))
          )
          Spacer(modifier = Modifier.height(6.dp))

          ExposedDropdownMenuBox(
            expanded = templateDropdownOpen,
            onExpandedChange = { templateDropdownOpen = !templateDropdownOpen },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = selectedTemplate?.title ?: "Fees Reminder (Hindi)",
              onValueChange = {},
              readOnly = true,
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateDropdownOpen) },
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
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
              expanded = templateDropdownOpen,
              onDismissRequest = { templateDropdownOpen = false },
              modifier = Modifier.background(Color(0xFF161B26))
            ) {
              templates.forEach { tmpl ->
                DropdownMenuItem(
                  text = { Text(text = tmpl.title, color = Color.White) },
                  onClick = {
                    selectedTemplate = tmpl
                    templateDropdownOpen = false
                  }
                )
              }
            }
          }
        }
      }

      // Message Preview Box matching Screenshot 11
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF131722),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            val templateRaw = selectedTemplate?.content ?: previewText
            val displayText = if (targetMember != null) {
              templateRaw
                .replace("{name}", targetMember.name)
                .replace("{amount}", targetMember.dueAmount.toString())
                .replace("{gym_name}", settings.gymName)
                .replace("{plan}", targetMember.planName)
                .replace("{expiry}", targetMember.expiryDate)
                .replace("{method}", "UPI")
            } else {
              templateRaw
                .replace("{name}", "सदस्य / Member")
                .replace("{amount}", "500")
                .replace("{gym_name}", settings.gymName)
                .replace("{plan}", "General Plan")
                .replace("{expiry}", "30 Days")
                .replace("{method}", "UPI")
            }

            Text(
              text = displayText,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                lineHeight = 22.sp,
                fontSize = 13.sp
              )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              Text(
                text = "${displayText.length}/1000",
                color = Color(0xFF64748B),
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MemberCheckRow(
  title: String,
  checked: Boolean,
  onToggle: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onToggle)
      .padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = { onToggle() },
      colors = CheckboxDefaults.colors(
        checkedColor = Color(0xFF00C853),
        checkmarkColor = Color.White,
        uncheckedColor = Color(0xFF64748B)
      )
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.bodyMedium.copy(
        color = Color.White,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
      )
    )
  }
}
