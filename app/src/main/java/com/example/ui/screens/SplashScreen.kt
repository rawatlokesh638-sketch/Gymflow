package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel

@Composable
fun SplashScreen(
  viewModel: FitProViewModel,
  onLoginSuccess: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0B0E14))
  ) {
    // Hero gym wallpaper image with dark cinematic gradient overlay matching Screenshot 1
    Image(
      painter = painterResource(id = R.drawable.img_gym_hero),
      contentDescription = "FitPro Gym Hero",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .alpha(0.35f)
    )

    // Gradient Scrim
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0x660B0E14),
              Color(0xCC0B0E14),
              Color(0xFF0B0E14)
            )
          )
        )
    )

    // Content matching Screenshot 1
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Dumbbell Logo + Title + Subtitle + 5 Checkmarks
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Green dumbbell icon matching Screenshot 1
        Icon(
          imageVector = Icons.Filled.FitnessCenter,
          contentDescription = "FitPro",
          tint = Color(0xFF00C853),
          modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "FitPro",
          style = MaterialTheme.typography.displayLarge.copy(
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            letterSpacing = 0.5.sp
          )
        )

        Text(
          text = "Gym Management\nMade Simple",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
          ),
          modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 5 Bullet points with circular green checkmark badges matching Screenshot 1
        val features = listOf(
          "Members Management",
          "Attendance (QR/Manual)",
          "Fees & Payments",
          "WhatsApp Reminders",
          "Reports & Growth"
        )

        Column(
          verticalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
          features.forEach { feature ->
            Row(
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF00C853)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(13.dp)
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Text(
                text = feature,
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Medium,
                  fontSize = 14.sp
                )
              )
            }
          }
        }
      }

      // Bottom Buttons & Language Toggle matching Screenshot 1
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 10.dp)
      ) {
        Button(
          onClick = {
            viewModel.login()
            onLoginSuccess()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00C853), // Solid green matching screenshot
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(26.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("login_continue_button")
        ) {
          Text(
            text = "Login to Continue",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 15.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = "New here? ",
            color = Color(0xFF8E9BAE),
            fontSize = 13.sp
          )
          Text(
            text = "Create Account",
            color = Color(0xFF00C853),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
              .clickable {
                viewModel.login()
                onLoginSuccess()
              }
              .testTag("create_account_button")
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // "हिंदी | English" centered at the very bottom matching Screenshot 1
        Text(
          text = "हिंदी  |  English",
          color = Color(0xFF8E9BAE),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier
            .clickable { viewModel.toggleLanguage() }
            .testTag("splash_lang_toggle")
        )
      }
    }
  }
}
