package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.FitProViewModel
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.SubScreen

class MainActivity : ComponentActivity() {

  private val viewModel: FitProViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        FitProApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun FitProApp(viewModel: FitProViewModel) {
  val isLoggedIn by viewModel.isLoggedIn.collectAsState()
  val activeTab by viewModel.activeTab.collectAsState()
  val activeSubScreen by viewModel.activeSubScreen.collectAsState()
  val snackbarMessage by viewModel.snackbarMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  // Handle snackbar messages
  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let {
      snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
      viewModel.clearSnackbar()
    }
  }

  // Back handling
  BackHandler(enabled = activeSubScreen != SubScreen.NONE || activeTab != NavigationTab.HOME) {
    if (activeSubScreen != SubScreen.NONE) {
      viewModel.closeSubScreen()
    } else if (activeTab != NavigationTab.HOME) {
      viewModel.setTab(NavigationTab.HOME)
    }
  }

  Scaffold(
    containerColor = GymFlowBackground,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
          .navigationBarsPadding()
          .padding(bottom = if (activeSubScreen == SubScreen.NONE) 70.dp else 16.dp)
      ) { data ->
        Snackbar(
          snackbarData = data,
          containerColor = GymFlowTextPrimary,
          contentColor = Color.White,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .border(1.dp, GymFlowPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp)
        )
      }
    },
    bottomBar = {
      if (isLoggedIn && activeSubScreen == SubScreen.NONE) {
        FitProBottomNavigationBar(
          activeTab = activeTab,
          onTabSelected = { viewModel.setTab(it) }
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (!isLoggedIn) {
        SplashScreen(
          viewModel = viewModel,
          onLoginSuccess = { viewModel.login() }
        )
      } else {
        // High fidelity animated transitions between screens
        AnimatedContent(
          targetState = activeSubScreen,
          transitionSpec = {
            if (targetState != SubScreen.NONE) {
              slideInVertically(
                initialOffsetY = { 80 },
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
              ) + fadeIn(tween(250)) togetherWith
                slideOutVertically(
                  targetOffsetY = { -40 },
                  animationSpec = tween(200)
                ) + fadeOut(tween(200))
            } else {
              fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            }
          },
          label = "subscreen_transition"
        ) { subScreen ->
          when (subScreen) {
            SubScreen.MEMBER_PROFILE -> {
              MemberProfileScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeSubScreen() }
              )
            }
            SubScreen.ADD_MEMBER -> {
              AddEditMemberScreen(
                viewModel = viewModel,
                memberToEditId = null,
                onBack = { viewModel.closeSubScreen() }
              )
            }
            SubScreen.EDIT_MEMBER -> {
              val selId by viewModel.selectedMemberId.collectAsState()
              AddEditMemberScreen(
                viewModel = viewModel,
                memberToEditId = selId,
                onBack = { viewModel.closeSubScreen() }
              )
            }
            SubScreen.COLLECT_PAYMENT -> {
              CollectPaymentModalScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeSubScreen() }
              )
            }
            SubScreen.WHATSAPP_REMINDER -> {
              WhatsAppScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeSubScreen() }
              )
            }
            SubScreen.REPORTS -> {
              ReportsScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeSubScreen() }
              )
            }
            SubScreen.PLANS_PRICING -> {
              PlansPricingScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeSubScreen() }
              )
            }
            SubScreen.SUBSCRIPTIONS -> {
              AppSubscriptionModalScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeSubScreen() }
              )
            }
            SubScreen.DUE_MEMBERS -> {
              PaymentsScreen(viewModel = viewModel)
            }
            SubScreen.ATTENDANCE_LIST -> {
              AttendanceScreen(viewModel = viewModel)
            }
            SubScreen.NONE -> {
              // Main Tab Navigation with smooth AnimatedContent
              AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                  fadeIn(animationSpec = tween(180)) togetherWith
                    fadeOut(animationSpec = tween(140))
                },
                label = "tab_transition"
              ) { tab ->
                when (tab) {
                  NavigationTab.HOME -> DashboardScreen(viewModel = viewModel)
                  NavigationTab.MEMBERS -> MembersScreen(viewModel = viewModel)
                  NavigationTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                  NavigationTab.PAYMENTS -> PaymentsScreen(viewModel = viewModel)
                  NavigationTab.MORE -> SettingsScreen(viewModel = viewModel)
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
fun FitProBottomNavigationBar(
  activeTab: NavigationTab,
  onTabSelected: (NavigationTab) -> Unit
) {
  Surface(
    color = GymFlowSurface,
    tonalElevation = 4.dp,
    shadowElevation = 8.dp,
    border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val navItems = listOf(
        NavigationItem(NavigationTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem(NavigationTab.MEMBERS, "Members", Icons.Filled.PeopleAlt, Icons.Outlined.PeopleAlt),
        NavigationItem(NavigationTab.ATTENDANCE, "Attendance", Icons.Filled.AssignmentTurnedIn, Icons.Outlined.AssignmentTurnedIn),
        NavigationItem(NavigationTab.PAYMENTS, "Payments", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
        NavigationItem(NavigationTab.MORE, "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
      )

      navItems.forEach { item ->
        val isSelected = activeTab == item.tab
        val tabColor = if (isSelected) GymFlowPrimary else GymFlowTextSecondary

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(bounded = false, radius = 24.dp, color = GymFlowPrimary.copy(alpha = 0.2f)),
              onClick = { onTabSelected(item.tab) }
            )
            .padding(vertical = 4.dp)
            .testTag("nav_tab_${item.label.lowercase()}")
        ) {
          Icon(
            imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
            contentDescription = item.label,
            tint = tabColor,
            modifier = Modifier.size(24.dp)
          )

          Spacer(modifier = Modifier.height(3.dp))

          Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
              color = tabColor,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 10.sp
            )
          )
        }
      }
    }
  }
}

private data class NavigationItem(
  val tab: NavigationTab,
  val label: String,
  val activeIcon: ImageVector,
  val inactiveIcon: ImageVector
)
