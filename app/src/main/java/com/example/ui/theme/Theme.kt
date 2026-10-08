package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GymFlowLightColorScheme = lightColorScheme(
  primary = GymFlowPrimary,
  onPrimary = GymFlowOnPrimary,
  primaryContainer = GymFlowPrimaryContainer,
  onPrimaryContainer = GymFlowOnPrimaryContainer,
  secondary = GymFlowInfo,
  onSecondary = Color.White,
  secondaryContainer = GymFlowInfoContainer,
  onSecondaryContainer = GymFlowOnInfoContainer,
  tertiary = GymFlowWarning,
  onTertiary = Color.White,
  tertiaryContainer = GymFlowWarningContainer,
  onTertiaryContainer = GymFlowOnWarningContainer,
  background = GymFlowBackground,
  onBackground = GymFlowTextPrimary,
  surface = GymFlowSurface,
  onSurface = GymFlowTextPrimary,
  surfaceVariant = GymFlowSurfaceVariant,
  onSurfaceVariant = GymFlowTextSecondary,
  error = GymFlowDanger,
  onError = Color.White,
  errorContainer = GymFlowDangerContainer,
  onErrorContainer = GymFlowOnDangerContainer,
  outline = GymFlowSurfaceBorder,
  outlineVariant = GymFlowDivider
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  val colorScheme = GymFlowLightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
