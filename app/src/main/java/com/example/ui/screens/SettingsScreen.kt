package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel
import com.example.viewmodel.SubScreen

@Composable
fun SettingsScreen(
  viewModel: FitProViewModel
) {
  val settings by viewModel.settings.collectAsState()
  var showLogoutDialog by remember { mutableStateOf(false) }
  var showResetDialog by remember { mutableStateOf(false) }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      containerColor = Color(0xFF161B26),
      title = { Text("Reset Data to 0?", color = Color.White, fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to clear all members, payments, and attendance? The app will start fresh with completely real 0 data.", color = Color(0xFFCBD5E1)) },
      confirmButton = {
        Button(
          onClick = {
            showResetDialog = false
            viewModel.resetAllDataToZero()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
        ) {
          Text("Reset to 0", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Cancel", color = Color(0xFF8E9BAE))
        }
      }
    )
  }

  if (showLogoutDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutDialog = false },
      containerColor = Color(0xFF161B26),
      title = { Text("Logout?", color = Color.White, fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to log out of FitPro Gym Manager?", color = Color(0xFFCBD5E1)) },
      confirmButton = {
        Button(
          onClick = {
            showLogoutDialog = false
            viewModel.logout()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
        ) {
          Text("Logout", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutDialog = false }) {
          Text("Cancel", color = Color(0xFF8E9BAE))
        }
      }
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0B0E14))
      .testTag("settings_screen"),
    contentPadding = PaddingValues(bottom = 120.dp)
  ) {
    // Header Profile Card matching Screenshot 13: "GymPro Gym", "Dharuhera, Haryana", edit pencil
    item {
      Surface(
        color = Color(0xFF0B0E14),
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(Color(0xFF263238))
              .border(1.dp, Color(0xFF00C853), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.FitnessCenter,
              contentDescription = null,
              tint = Color(0xFF00C853),
              modifier = Modifier.size(26.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "GymPro Gym",
              style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
              )
            )
            Text(
              text = "Dharuhera, Haryana",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF8E9BAE),
                fontSize = 13.sp
              )
            )
          }

          IconButton(onClick = { viewModel.showSnackbar("Edit Gym Profile") }) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit",
              tint = Color(0xFF8E9BAE),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // Menu List matching Screenshot 13
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF131722),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        Column {
          SettingsRowItem(icon = Icons.Outlined.Business, title = "Gym Details", onClick = { viewModel.showSnackbar("Gym Details: GymPro Gym") })
          HorizontalDivider(color = Color(0xFF1E2433))
          SettingsRowItem(icon = Icons.Outlined.Sell, title = "Plans & Pricing", onClick = { viewModel.openSubScreen(SubScreen.PLANS_PRICING) })
          HorizontalDivider(color = Color(0xFF1E2433))
          SettingsRowItem(icon = Icons.Outlined.Chat, title = "WhatsApp Settings", onClick = { viewModel.openSubScreen(SubScreen.WHATSAPP_REMINDER) })
          HorizontalDivider(color = Color(0xFF1E2433))
          SettingsRowItem(icon = Icons.Outlined.CreditCard, title = "Payment Settings (UPI)", onClick = { viewModel.showSnackbar("UPI: ${settings.upiId}") })
          HorizontalDivider(color = Color(0xFF1E2433))
          SettingsRowItem(icon = Icons.Outlined.Badge, title = "Staff / Trainer Access", onClick = { viewModel.showSnackbar("Staff access settings") })
          HorizontalDivider(color = Color(0xFF1E2433))

          // Backup & Offline Mode with Switch ON matching Screenshot 13
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Outlined.CloudSync, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = "Backup & Offline Mode",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
              )
            }
            Switch(
              checked = settings.isOfflineBackupEnabled,
              onCheckedChange = { viewModel.toggleOfflineBackup() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00C853)
              )
            )
          }

          HorizontalDivider(color = Color(0xFF1E2433))

          // Language matching Screenshot 13: "Language" ... "हिंदी >"
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.toggleLanguage() }
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Outlined.Translate, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = "Language",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "हिंदी",
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
            }
          }

          HorizontalDivider(color = Color(0xFF1E2433))
          SettingsRowItem(icon = Icons.Outlined.Notifications, title = "Notifications", onClick = { viewModel.showSnackbar("Notifications enabled") })
          HorizontalDivider(color = Color(0xFF1E2433))

          // Subscription matching Screenshot 13: "Subscription", "Pro Plan Valid till 12 Dec 2027 >"
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.openSubScreen(SubScreen.SUBSCRIPTIONS) }
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = "Subscription",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Pro Plan",
                  color = Color(0xFF00E676),
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                Text(
                  text = "Valid till 12 Dec 2027",
                  color = Color(0xFF8E9BAE),
                  fontSize = 10.sp
                )
              }
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
            }
          }

          HorizontalDivider(color = Color(0xFF1E2433))
          SettingsRowItem(icon = Icons.Outlined.HelpOutline, title = "Help & Support", onClick = { viewModel.showSnackbar("Help & Support: support@fitpro.in") })
          HorizontalDivider(color = Color(0xFF1E2433))
          SettingsRowItem(
            icon = Icons.Outlined.RestartAlt,
            title = "Reset All Data (Start Fresh from 0)",
            onClick = { showResetDialog = true }
          )
          HorizontalDivider(color = Color(0xFF1E2433))

          // Logout in red matching Screenshot 13
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showLogoutDialog = true }
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Outlined.Logout, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(
              text = "Logout",
              color = Color(0xFFFF5252),
              fontWeight = FontWeight.Medium,
              fontSize = 14.sp
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SettingsRowItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(22.dp))
      Spacer(modifier = Modifier.width(14.dp))
      Text(text = title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
  }
}
