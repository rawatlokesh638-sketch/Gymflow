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
import com.example.model.AttendanceMethod
import com.example.ui.components.FitProTopBar
import com.example.ui.components.InteractiveLaserQrScanner
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel
import com.example.viewmodel.SubScreen

@Composable
fun AttendanceScreen(
  viewModel: FitProViewModel
) {
  val attendanceList by viewModel.attendance.collectAsState()
  val activeTab by viewModel.attendanceTab.collectAsState()
  val mobileInput by viewModel.mobileCheckInInput.collectAsState()

  var attendanceSearch by remember { mutableStateOf("") }
  var viewMode by remember { mutableStateOf("QR_VIEW") } // "QR_VIEW" (Screen 6) vs "LIST_VIEW" (Screen 7)

  val members by viewModel.members.collectAsState()
  val presentCount = attendanceList.size
  val absentCount = maxOf(0, members.size - presentCount)

  val todayDateFormatted = remember {
    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0B0E14))
      .testTag("attendance_screen"),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
    if (viewMode == "QR_VIEW") {
      // SCREEN 6: ATTENDANCE (QR)
      item {
        FitProTopBar(
          title = "Attendance",
          subtitle = null,
          actions = {
            IconButton(onClick = { viewMode = "LIST_VIEW" }) {
              Icon(Icons.Default.FormatListBulleted, contentDescription = "List", tint = Color(0xFF00C853))
            }
          }
        )
      }

      // Tabs at top matching Screenshot 6: [Manual] [QR Code] [Offline]
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Manual", "QR Code", "Offline").forEach { label ->
            val key = label.uppercase().replace(" ", "_")
            val isSelected = activeTab == key || (label == "QR Code" && activeTab == "QR_CODE")
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) Color(0xFF00C853) else Color(0xFF131722),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) Color(0xFF00C853) else Color(0xFF1E2433)
              ),
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .clickable { viewModel.setAttendanceTab(key) }
                .testTag("attendance_tab_$key")
            ) {
              Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF8E9BAE),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            }
          }
        }
      }

      // Large Interactive QR Card matching Screenshot 6
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          InteractiveLaserQrScanner(
            modifier = Modifier.testTag("attendance_qr_canvas")
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Scan this QR at Reception\nfor Member Check-in",
            style = MaterialTheme.typography.titleMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              lineHeight = 20.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }

      // Or Enter Mobile Number Input & Button matching Screenshot 6
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Text(
            text = "Or Enter Mobile Number",
            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFCBD5E1)),
            modifier = Modifier.padding(bottom = 6.dp)
          )

          OutlinedTextField(
            value = mobileInput,
            onValueChange = { viewModel.setMobileCheckInInput(it) },
            placeholder = { Text("10 digit mobile number", color = Color(0xFF64748B), fontSize = 13.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
              .testTag("attendance_mobile_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { viewModel.submitMobileCheckIn() },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF00C853),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("attendance_checkin_button")
          ) {
            Text(
              text = "Check In",
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 15.sp
            )
          }
        }
      }

      // Today's Attendance preview list matching Screenshot 6
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Today's Attendance ($presentCount)",
            style = MaterialTheme.typography.titleMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          )
          Text(
            text = "Full List",
            color = Color(0xFF00C853),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { viewMode = "LIST_VIEW" }
          )
        }
      }

      if (attendanceList.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text("No check-ins today yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text("Members who check in will be listed here.", color = Color(0xFF8E9BAE), fontSize = 12.sp)
            }
          }
        }
      } else {
        items(attendanceList.take(3)) { item ->
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
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF263238)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = item.memberName.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = item.memberName,
                  style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                )
              }

              Text(
                text = item.checkInTime,
                color = Color(0xFF8E9BAE),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    } else {
      // SCREEN 7: TODAY'S ATTENDANCE LIST
      item {
        FitProTopBar(
          title = "Today's Attendance",
          subtitle = null,
          showBack = true,
          onBackClick = { viewMode = "QR_VIEW" },
          actions = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(end = 4.dp)
            ) {
              Text(todayDateFormatted, color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Medium)
              Spacer(modifier = Modifier.width(6.dp))
              Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
            }
          }
        )
      }

      // 2 Summary cards: Present (28) in green, Absent (6) in red matching Screenshot 7
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier.weight(1f)
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF0E3823)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(text = "Present", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                Text(text = "$presentCount", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 20.sp)
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier.weight(1f)
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF381419)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.PersonOff, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(text = "Absent", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                Text(text = "$absentCount", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 20.sp)
              }
            }
          }
        }
      }

      // Search bar matching Screenshot 7
      item {
        OutlinedTextField(
          value = attendanceSearch,
          onValueChange = { attendanceSearch = it },
          placeholder = { Text("Search member...", color = Color(0xFF64748B), fontSize = 13.sp) },
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

      // Member Rows matching Screenshot 7
      val filteredList = attendanceList.filter {
        attendanceSearch.isBlank() || it.memberName.contains(attendanceSearch, ignoreCase = true)
      }

      if (filteredList.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text("No attendance recorded today", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text("Check in members using QR or phone number", color = Color(0xFF8E9BAE), fontSize = 12.sp)
            }
          }
        }
      } else {
        items(filteredList) { record ->
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2433)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
              .clickable { viewModel.openSubScreen(SubScreen.MEMBER_PROFILE, record.memberId) }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    text = record.memberName.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = record.memberName,
                    style = MaterialTheme.typography.titleSmall.copy(
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                  )
                  Text(
                    text = record.planName,
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = Color(0xFF8E9BAE),
                      fontSize = 11.sp
                    )
                  )
                }
              }

              Text(
                text = record.checkInTime,
                color = Color(0xFF8E9BAE),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }
  }
}
