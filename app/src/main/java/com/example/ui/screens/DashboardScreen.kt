package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DashboardMetrics
import com.example.data.DueMemberItem
import com.example.data.ExpiringMemberItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.DateUtils
import com.example.viewmodel.FitProViewModel
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.SubScreen

@Composable
fun DashboardScreen(
  viewModel: FitProViewModel
) {
  val context = LocalContext.current
  val metrics by viewModel.dashboardMetrics.collectAsState()
  val settings by viewModel.settings.collectAsState()
  val isOffline by viewModel.isOffline.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  // Dynamic system dates & greeting
  val todayHeaderStr = remember { DateUtils.getTodayLongHeader() }
  val greeting = remember { DateUtils.getGreeting() }

  // Modals state
  var showAddMemberDialog by remember { mutableStateOf(false) }
  var showCollectDialog by remember { mutableStateOf(false) }
  var collectTargetMember by remember { mutableStateOf<DueMemberItem?>(null) }
  var showAllDueDialog by remember { mutableStateOf(false) }
  var showAllExpiringDialog by remember { mutableStateOf(false) }
  var showGymSettingsDialog by remember { mutableStateOf(false) }
  var showNotificationsDialog by remember { mutableStateOf(false) }

  // Refresh spinner animation
  val infiniteTransition = rememberInfiniteTransition(label = "refresh")
  val rotateAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "spin"
  )

  Scaffold(
    containerColor = GymFlowBackground,
    topBar = {
      // Top Navigation Header
      Surface(
        color = GymFlowSurface,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // GymFlow Brand Emblem
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GymFlowPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = "GymFlow",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Gym Name & Location
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = settings.gymName,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = GymFlowTextPrimary,
                    fontSize = 17.sp
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = GymFlowPrimaryLight
                ) {
                  Text(
                    text = "SaaS",
                    color = GymFlowPrimaryDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
              }

              Text(
                text = settings.gymLocation,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = GymFlowTextSecondary,
                  fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Online/Offline Status Chip
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isOffline) GymFlowWarningContainer else GymFlowSuccessContainer,
              modifier = Modifier
                .clickable { viewModel.toggleOffline() }
                .padding(end = 6.dp)
                .testTag("offline_toggle_chip")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOffline) GymFlowWarning else GymFlowSuccess)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isOffline) "Offline" else "Online",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isOffline) GymFlowOnWarningContainer else GymFlowOnSuccessContainer
                )
              }
            }

            // Refresh Button
            IconButton(
              onClick = { viewModel.refreshDashboard() },
              modifier = Modifier
                .size(40.dp)
                .testTag("dashboard_refresh_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = GymFlowTextSecondary,
                modifier = if (isLoading) Modifier.rotate(rotateAnim) else Modifier
              )
            }

            // Notifications Icon
            BadgedBox(
              badge = {
                val alertCount = metrics.dueMembersCount + metrics.expiringSoonCount
                if (alertCount > 0) {
                  Badge(
                    containerColor = GymFlowDanger,
                    contentColor = Color.White
                  ) {
                    Text("$alertCount", fontSize = 10.sp)
                  }
                }
              }
            ) {
              IconButton(
                onClick = { showNotificationsDialog = true },
                modifier = Modifier
                  .size(40.dp)
                  .testTag("dashboard_notifications_button")
              ) {
                Icon(
                  imageVector = Icons.Outlined.Notifications,
                  contentDescription = "Notifications",
                  tint = GymFlowTextPrimary
                )
              }
            }

            // Menu / Profile Settings Icon
            IconButton(
              onClick = { showGymSettingsDialog = true },
              modifier = Modifier
                .size(40.dp)
                .testTag("dashboard_profile_menu_button")
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = GymFlowTextPrimary
              )
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("dashboard_scroll_container"),
      contentPadding = PaddingValues(bottom = 100.dp)
    ) {
      // 1. Offline Banner (if offline)
      if (isOffline) {
        item {
          Surface(
            color = GymFlowWarningContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = GymFlowOnWarningContainer,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Offline Mode: Showing cached dashboard metrics.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GymFlowOnWarningContainer
              )
            }
          }
        }
      }

      // 2. Greeting & Dynamic Date Header
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = greeting,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = GymFlowTextPrimary,
                  fontSize = 22.sp
                )
              )
              Text(
                text = "Here's your gym overview",
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = GymFlowTextSecondary,
                  fontSize = 14.sp
                )
              )
            }

            // Quick Add Member action button in header
            Button(
              onClick = { showAddMemberDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
              modifier = Modifier
                .height(44.dp)
                .testTag("header_add_member_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Member", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Dynamic Date Pill
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = GymFlowSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = GymFlowPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = todayHeaderStr,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = GymFlowTextPrimary,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp
                )
              )
            }
          }
        }
      }

      // 3. Error State (if any)
      if (metrics.errorMessage != null) {
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = GymFlowDangerContainer,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("error_state_card")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.ErrorOutline,
                  contentDescription = null,
                  tint = GymFlowDanger,
                  modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Couldn't load dashboard data",
                    fontWeight = FontWeight.Bold,
                    color = GymFlowOnDangerContainer,
                    fontSize = 14.sp
                  )
                  Text(
                    text = "Please check connectivity & try again",
                    fontSize = 12.sp,
                    color = GymFlowOnDangerContainer.copy(alpha = 0.8f)
                  )
                }
              }

              Button(
                onClick = { viewModel.retryDashboard() },
                colors = ButtonDefaults.buttonColors(containerColor = GymFlowDanger),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // 4. Loading State Skeleton (if loading)
      if (isLoading) {
        item {
          DashboardSkeletonLoading()
        }
      } else {
        // 5. FIVE MAIN KPI CARDS (2x2 Grid + Today's Collection / Expiring cards)
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Row 1: Total Members & Active Members
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // CARD 1 — TOTAL MEMBERS
              StatCard(
                title = "Total Members",
                value = "${metrics.totalMembers}",
                subtitle = "All registered members",
                icon = Icons.Filled.PeopleAlt,
                iconBgColor = GymFlowPrimaryContainer,
                iconTint = GymFlowPrimary,
                valueColor = GymFlowTextPrimary,
                modifier = Modifier
                  .weight(1f)
                  .testTag("stat_total_members"),
                onClick = { viewModel.setTab(NavigationTab.MEMBERS) }
              )

              // CARD 2 — ACTIVE MEMBERS
              StatCard(
                title = "Active Members",
                value = "${metrics.activeMembers}",
                subtitle = "${metrics.activePercentage}% of total members",
                icon = Icons.Filled.CheckCircle,
                iconBgColor = GymFlowSuccessContainer,
                iconTint = GymFlowSuccess,
                valueColor = GymFlowTextPrimary,
                modifier = Modifier
                  .weight(1f)
                  .testTag("stat_active_members"),
                onClick = {
                  viewModel.setMemberFilter("ACTIVE")
                  viewModel.setTab(NavigationTab.MEMBERS)
                }
              )
            }

            // Row 2: Due Members (Highlighted) & Today's Collection
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // CARD 3 — DUE MEMBERS (Visually prominent for fee recovery)
              StatCard(
                title = "Due Members",
                value = "${metrics.dueMembersCount}",
                subtitle = if (metrics.dueMembersCount > 0) "${metrics.formattedTotalDue} pending" else "Payment clear",
                icon = Icons.Filled.PriorityHigh,
                iconBgColor = if (metrics.dueMembersCount > 0) GymFlowDangerContainer else GymFlowSurfaceVariant,
                iconTint = if (metrics.dueMembersCount > 0) GymFlowDanger else GymFlowTextSecondary,
                valueColor = if (metrics.dueMembersCount > 0) GymFlowDanger else GymFlowTextPrimary,
                isHighlighted = metrics.dueMembersCount > 0,
                highlightBorderColor = GymFlowDanger,
                modifier = Modifier
                  .weight(1f)
                  .testTag("stat_due_members"),
                onClick = {
                  if (metrics.dueMembersCount > 0) {
                    showAllDueDialog = true
                  } else {
                    viewModel.showSnackbar("No due members. All payments clear!")
                  }
                }
              )

              // CARD 4 — TODAY'S COLLECTION
              StatCard(
                title = "Today's Collection",
                value = metrics.formattedTodayTotal,
                subtitle = "Today",
                trend = if (metrics.todayCollectionTotal > 0) metrics.collectionTrendText else null,
                isPositiveTrend = metrics.isPositiveTrend,
                icon = Icons.Filled.AccountBalanceWallet,
                iconBgColor = GymFlowInfoContainer,
                iconTint = GymFlowInfo,
                valueColor = GymFlowTextPrimary,
                modifier = Modifier
                  .weight(1f)
                  .testTag("stat_today_collection"),
                onClick = {
                  viewModel.showSnackbar("Today's Total: ${metrics.formattedTodayTotal} (Cash: ${metrics.formattedCash}, UPI: ${metrics.formattedUpi}, Card: ${metrics.formattedCard})")
                }
              )
            }

            // CARD 5 — EXPIRING MEMBERSHIPS (Full width on mobile)
            StatCard(
              title = "Expiring Soon",
              value = "${metrics.expiringSoonCount}",
              subtitle = "Within 7 days • Next 7 days",
              trend = if (metrics.expiringSoonCount > 0) "Action Needed" else null,
              isPositiveTrend = false,
              icon = Icons.Filled.HourglassEmpty,
              iconBgColor = GymFlowWarningContainer,
              iconTint = GymFlowWarning,
              valueColor = if (metrics.expiringSoonCount > 0) GymFlowWarning else GymFlowTextPrimary,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("stat_expiring_soon"),
              onClick = {
                if (metrics.expiringSoonCount > 0) {
                  showAllExpiringDialog = true
                } else {
                  viewModel.showSnackbar("No memberships expiring within 7 days.")
                }
              }
            )
          }
        }

        // 6. Empty State CTA (if totalMembers == 0)
        if (metrics.totalMembers == 0) {
          item {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = GymFlowSurface,
              border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
              tonalElevation = 2.dp,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("empty_state_card")
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Box(
                  modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GymFlowPrimaryLight),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = GymFlowPrimary,
                    modifier = Modifier.size(32.dp)
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                  text = "Welcome to GymFlow!",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTextPrimary,
                    fontSize = 17.sp
                  )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = "No members registered yet. Add your first member to start tracking memberships, dues, and daily collections.",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = GymFlowTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                  ),
                  modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                  onClick = { showAddMemberDialog = true },
                  colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("empty_cta_add_member")
                ) {
                  Icon(Icons.Default.Add, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Add your first member", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                  onClick = { viewModel.seedSampleData() },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("empty_cta_load_demo")
                ) {
                  Icon(Icons.Default.Dataset, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Load Demo Data (PowerFit Gym, Dharuhera)", fontSize = 13.sp)
                }
              }
            }
          }
        }

        // 7. SECTION 6: QUICK SUMMARY SECTION — "Membership Overview"
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = GymFlowSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
            tonalElevation = 2.dp,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("section_membership_overview")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Text(
                text = "Membership Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = GymFlowTextPrimary,
                  fontSize = 16.sp
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Horizontal segmented visual progress representation
              val total = maxOf(1, metrics.totalMembers)
              val activeWeight = maxOf(0.01f, metrics.activeMembers.toFloat() / total)
              val dueWeight = maxOf(0.01f, metrics.dueMembersCount.toFloat() / total)
              val expiringWeight = maxOf(0.01f, metrics.expiringSoonCount.toFloat() / total)
              val expiredWeight = maxOf(0.01f, metrics.expiredCount.toFloat() / total)

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(12.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(GymFlowSurfaceVariant)
              ) {
                if (metrics.activeMembers > 0) {
                  Box(
                    modifier = Modifier
                      .weight(activeWeight)
                      .fillMaxHeight()
                      .background(GymFlowSuccess)
                  )
                }
                if (metrics.dueMembersCount > 0) {
                  Box(
                    modifier = Modifier
                      .weight(dueWeight)
                      .fillMaxHeight()
                      .background(GymFlowDanger)
                  )
                }
                if (metrics.expiringSoonCount > 0) {
                  Box(
                    modifier = Modifier
                      .weight(expiringWeight)
                      .fillMaxHeight()
                      .background(GymFlowWarning)
                  )
                }
                if (metrics.expiredCount > 0) {
                  Box(
                    modifier = Modifier
                      .weight(expiredWeight)
                      .fillMaxHeight()
                      .background(GymFlowTextMuted)
                  )
                }
                if (metrics.totalMembers == 0) {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(GymFlowSurfaceVariant)
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // 4 Metrics Grid
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                OverviewStatItem(label = "Active", count = metrics.activeMembers, color = GymFlowSuccess)
                OverviewStatItem(label = "Due", count = metrics.dueMembersCount, color = GymFlowDanger)
                OverviewStatItem(label = "Expiring", count = metrics.expiringSoonCount, color = GymFlowWarning)
                OverviewStatItem(label = "Expired", count = metrics.expiredCount, color = GymFlowTextMuted)
              }
            }
          }
        }

        // 8. SECTION 7: TODAY'S COLLECTION SUMMARY
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = GymFlowSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
            tonalElevation = 2.dp,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp)
              .testTag("section_today_collection")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Today's Collection",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = GymFlowTextPrimary,
                      fontSize = 16.sp
                    )
                  )
                  Text(
                    text = if (metrics.todayCollectionTotal == 0) "No payments collected today" else "Grouped by payment method",
                    style = MaterialTheme.typography.bodySmall.copy(color = GymFlowTextSecondary)
                  )
                }

                Text(
                  text = metrics.formattedTodayTotal,
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = GymFlowPrimary,
                    fontSize = 20.sp
                  )
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              if (metrics.todayCollectionTotal == 0) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = GymFlowSurfaceVariant,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      Icons.Default.Info,
                      contentDescription = null,
                      tint = GymFlowTextSecondary,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "₹0 collected today. Tap 'Collect Payment' to record reception fees.",
                      fontSize = 12.sp,
                      color = GymFlowTextSecondary
                    )
                  }
                }
              } else {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  CollectionPill(
                    label = "Cash",
                    amount = metrics.formattedCash,
                    icon = Icons.Default.Money,
                    modifier = Modifier.weight(1f)
                  )
                  CollectionPill(
                    label = "UPI",
                    amount = metrics.formattedUpi,
                    icon = Icons.Default.QrCode2,
                    modifier = Modifier.weight(1f)
                  )
                  CollectionPill(
                    label = "Card",
                    amount = metrics.formattedCard,
                    icon = Icons.Default.CreditCard,
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
          }
        }

        // 9. SECTION 8: EXPIRING MEMBERS SECTION
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Expiring Soon",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTextPrimary,
                    fontSize = 16.sp
                  )
                )
                Text(
                  text = "Memberships expiring within 7 days (${metrics.expiringSoonCount})",
                  style = MaterialTheme.typography.bodySmall.copy(color = GymFlowTextSecondary)
                )
              }

              if (metrics.expiringSoonCount > 0) {
                TextButton(
                  onClick = { showAllExpiringDialog = true },
                  modifier = Modifier.testTag("view_all_expiring_button")
                ) {
                  Text("View All", color = GymFlowPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (metrics.expiringMembersList.isEmpty()) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = GymFlowSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = GymFlowSuccess)
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "No memberships expiring in the next 7 days.",
                    color = GymFlowTextSecondary,
                    fontSize = 13.sp
                  )
                }
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                metrics.expiringMembersList.take(3).forEach { item ->
                  ExpiringMemberRow(
                    item = item,
                    onRemind = { viewModel.sendReminder(context, item.memberId) }
                  )
                }
              }
            }
          }
        }

        // 10. SECTION 9: DUE MEMBERS PREVIEW
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Payment Due",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTextPrimary,
                    fontSize = 16.sp
                  )
                )
                Text(
                  text = "Top members with pending fees (${metrics.dueMembersCount})",
                  style = MaterialTheme.typography.bodySmall.copy(color = GymFlowTextSecondary)
                )
              }

              if (metrics.dueMembersCount > 0) {
                TextButton(
                  onClick = { showAllDueDialog = true },
                  modifier = Modifier.testTag("view_all_due_members_button")
                ) {
                  Text("View All Due Members", color = GymFlowPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (metrics.topDueMembersList.isEmpty()) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = GymFlowSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = GymFlowSuccess)
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "All member dues are clear! Outstanding balance is ₹0.",
                    color = GymFlowTextSecondary,
                    fontSize = 13.sp
                  )
                }
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                metrics.topDueMembersList.take(5).forEach { item ->
                  DueMemberRow(
                    item = item,
                    onCollect = {
                      collectTargetMember = item
                      showCollectDialog = true
                    },
                    onRemind = { viewModel.sendReminder(context, item.memberId) }
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Active Modals & Dialogs
  if (showAddMemberDialog) {
    AddMemberDialog(
      viewModel = viewModel,
      onDismiss = { showAddMemberDialog = false }
    )
  }

  if (showCollectDialog && collectTargetMember != null) {
    CollectPaymentDialog(
      viewModel = viewModel,
      memberId = collectTargetMember!!.memberId,
      memberName = collectTargetMember!!.name,
      dueAmount = collectTargetMember!!.dueAmount,
      onDismiss = {
        showCollectDialog = false
        collectTargetMember = null
      }
    )
  }

  if (showAllDueDialog) {
    AllDueMembersDialog(
      viewModel = viewModel,
      dueMembers = metrics.topDueMembersList,
      onDismiss = { showAllDueDialog = false },
      onCollect = { item ->
        showAllDueDialog = false
        collectTargetMember = item
        showCollectDialog = true
      }
    )
  }

  if (showAllExpiringDialog) {
    AllExpiringMembersDialog(
      viewModel = viewModel,
      expiringMembers = metrics.expiringMembersList,
      onDismiss = { showAllExpiringDialog = false }
    )
  }

  if (showGymSettingsDialog) {
    GymSettingsAndDataDialog(
      viewModel = viewModel,
      onDismiss = { showGymSettingsDialog = false }
    )
  }

  if (showNotificationsDialog) {
    NotificationsDialog(
      metrics = metrics,
      onDismiss = { showNotificationsDialog = false }
    )
  }
}

@Composable
private fun OverviewStatItem(
  label: String,
  count: Int,
  color: Color
) {
  Column(horizontalAlignment = Alignment.Start) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(
          color = GymFlowTextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = "$count",
      style = MaterialTheme.typography.titleMedium.copy(
        color = GymFlowTextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
      ),
      modifier = Modifier.padding(start = 14.dp)
    )
  }
}

@Composable
private fun CollectionPill(
  label: String,
  amount: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = GymFlowSurfaceVariant,
    border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(icon, contentDescription = null, tint = GymFlowPrimary, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = label, fontSize = 11.sp, color = GymFlowTextSecondary)
      Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GymFlowTextPrimary)
    }
  }
}

@Composable
private fun ExpiringMemberRow(
  item: ExpiringMemberItem,
  onRemind: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = GymFlowSurface,
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
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(GymFlowWarningContainer),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = item.avatarInitial,
            fontWeight = FontWeight.Bold,
            color = GymFlowOnWarningContainer,
            fontSize = 13.sp
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = item.name,
            fontWeight = FontWeight.Bold,
            color = GymFlowTextPrimary,
            fontSize = 14.sp
          )
          Text(
            text = "${item.planName} • ${item.statusText}",
            color = GymFlowWarning,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
          )
        }
      }

      Button(
        onClick = onRemind,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier
          .height(44.dp)
          .testTag("remind_button_${item.memberId}")
      ) {
        Text("Remind", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun DueMemberRow(
  item: DueMemberItem,
  onCollect: () -> Unit,
  onRemind: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = GymFlowSurface,
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
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(GymFlowDangerContainer),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = item.avatarInitial,
            fontWeight = FontWeight.Bold,
            color = GymFlowOnDangerContainer,
            fontSize = 13.sp
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = item.name,
            fontWeight = FontWeight.Bold,
            color = GymFlowTextPrimary,
            fontSize = 14.sp
          )
          Text(
            text = "₹${item.dueAmount} • ${item.daysOverdue} days overdue",
            color = GymFlowDanger,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(
          onClick = onRemind,
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier
            .height(44.dp)
            .testTag("remind_due_button_${item.memberId}")
        ) {
          Text("Remind", fontSize = 12.sp, color = GymFlowPrimary)
        }

        Button(
          onClick = onCollect,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .height(44.dp)
            .testTag("collect_due_button_${item.memberId}")
        ) {
          Text("Collect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun NotificationsDialog(
  metrics: DashboardMetrics,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", fontWeight = FontWeight.Bold, color = GymFlowPrimary)
      }
    },
    title = {
      Text("GymFlow Notifications", fontWeight = FontWeight.Bold, color = GymFlowTextPrimary)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (metrics.dueMembersCount > 0) {
          NotificationItem(
            icon = Icons.Default.Warning,
            iconTint = GymFlowDanger,
            title = "${metrics.dueMembersCount} Members have pending fees",
            subtitle = "Total pending recovery: ${metrics.formattedTotalDue}"
          )
        }
        if (metrics.expiringSoonCount > 0) {
          NotificationItem(
            icon = Icons.Default.HourglassEmpty,
            iconTint = GymFlowWarning,
            title = "${metrics.expiringSoonCount} Memberships expiring soon",
            subtitle = "Follow up within 7 days for renewals"
          )
        }
        NotificationItem(
          icon = Icons.Default.MonetizationOn,
          iconTint = GymFlowSuccess,
          title = "Today's Collection: ${metrics.formattedTodayTotal}",
          subtitle = "Cash: ${metrics.formattedCash} • UPI: ${metrics.formattedUpi} • Card: ${metrics.formattedCard}"
        )
      }
    }
  )
}

@Composable
private fun NotificationItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = GymFlowTextPrimary)
      Text(text = subtitle, fontSize = 11.sp, color = GymFlowTextSecondary)
    }
  }
}
