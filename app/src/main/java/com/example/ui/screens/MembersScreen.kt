package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MemberStatus
import com.example.ui.components.GymFlowTopBar
import com.example.ui.components.MemberRowCard
import com.example.ui.theme.*
import com.example.util.DateUtils
import com.example.viewmodel.FitProViewModel
import com.example.viewmodel.SubScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
  viewModel: FitProViewModel
) {
  val context = LocalContext.current
  val allMembers by viewModel.members.collectAsState()
  val filteredMembers by viewModel.filteredMembers.collectAsState()
  val statusFilter by viewModel.memberFilterStatus.collectAsState()
  val planFilter by viewModel.memberFilterPlanId.collectAsState()
  val dueFilter by viewModel.memberFilterDue.collectAsState()
  val expiryFilter by viewModel.memberFilterExpiry.collectAsState()
  val searchQuery by viewModel.memberSearchQuery.collectAsState()
  val activeFiltersCount by viewModel.activeFiltersCount.collectAsState()
  val plans by viewModel.plans.collectAsState()

  var showFilterBottomSheet by remember { mutableStateOf(false) }
  var isSearchExpanded by remember { mutableStateOf(searchQuery.isNotBlank()) }

  val nonDeletedMembers = remember(allMembers) { allMembers.filter { !it.deleted } }
  val totalCount = nonDeletedMembers.size
  val activeCount = nonDeletedMembers.count { it.status == MemberStatus.ACTIVE }
  val dueCount = nonDeletedMembers.count { it.dueAmount > 0 }
  val expiringCount = nonDeletedMembers.count {
    DateUtils.isExpiringWithin7Days(it.expiryDate) && !DateUtils.isExpired(it.expiryDate)
  }
  val expiredCount = nonDeletedMembers.count { DateUtils.isExpired(it.expiryDate) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(GymFlowBackground)
      .testTag("members_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 150.dp)
    ) {
      // 1. Mobile-First Top Header
      item {
        GymFlowTopBar(
          title = "Members",
          subtitle = "$totalCount Members registered",
          actions = {
            // Search toggle icon
            IconButton(
              onClick = {
                isSearchExpanded = !isSearchExpanded
                if (!isSearchExpanded) viewModel.setMemberSearchQuery("")
              },
              modifier = Modifier.testTag("members_search_toggle_button")
            ) {
              Icon(
                imageVector = if (isSearchExpanded) Icons.Default.SearchOff else Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isSearchExpanded || searchQuery.isNotEmpty()) GymFlowPrimary else GymFlowTextSecondary
              )
            }

            // Filter button with badge indicator
            BadgedBox(
              badge = {
                if (activeFiltersCount > 0) {
                  Badge(
                    containerColor = GymFlowPrimary,
                    contentColor = Color.White
                  ) {
                    Text(text = activeFiltersCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            ) {
              IconButton(
                onClick = { showFilterBottomSheet = true },
                modifier = Modifier.testTag("members_filter_button")
              ) {
                Icon(
                  imageVector = Icons.Default.FilterList,
                  contentDescription = "Filter",
                  tint = if (activeFiltersCount > 0) GymFlowPrimary else GymFlowTextSecondary
                )
              }
            }

            // Compact + Add Member icon in top bar
            IconButton(
              onClick = { viewModel.openSubScreen(SubScreen.ADD_MEMBER) },
              modifier = Modifier
                .padding(start = 4.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(GymFlowPrimary)
                .testTag("members_header_add_button")
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Member",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        )
      }

      // 2. Instant Search Bar (Animated visibility or permanent when active)
      if (isSearchExpanded || searchQuery.isNotEmpty()) {
        item {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setMemberSearchQuery(it) },
            placeholder = {
              Text(
                text = "Search by name, mobile, ID...",
                color = GymFlowTextMuted,
                fontSize = 14.sp
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = GymFlowPrimary
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.setMemberSearchQuery("") }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = GymFlowTextSecondary
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GymFlowPrimary,
              unfocusedBorderColor = GymFlowSurfaceBorder,
              focusedContainerColor = GymFlowSurface,
              unfocusedContainerColor = GymFlowSurface,
              focusedTextColor = GymFlowTextPrimary,
              unfocusedTextColor = GymFlowTextPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp)
              .testTag("members_search_input")
          )
        }
      }

      // 3. Horizontal Quick Filter Chips Bar
      item {
        val quickFilters = listOf(
          Triple("ALL", "All ($totalCount)", totalCount),
          Triple("ACTIVE", "Active ($activeCount)", activeCount),
          Triple("DUE", "Due ($dueCount)", dueCount),
          Triple("EXPIRING", "Expiring ($expiringCount)", expiringCount),
          Triple("EXPIRED", "Expired ($expiredCount)", expiredCount)
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          quickFilters.forEach { (key, label, _) ->
            val isSelected = statusFilter == key && planFilter == "ALL" && dueFilter == "ALL" && expiryFilter == "ALL"
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) GymFlowPrimary else GymFlowSurface,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) GymFlowPrimary else GymFlowSurfaceBorder
              ),
              shadowElevation = if (isSelected) 2.dp else 0.dp,
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                  viewModel.setMemberStatusFilter(key)
                }
                .testTag("quick_filter_$key")
            ) {
              Text(
                text = label,
                color = if (isSelected) Color.White else GymFlowTextPrimary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              )
            }
          }
        }
      }

      // 4. Active Filters Indicator bar (if extra filters like plan/due/expiry are active)
      if (activeFiltersCount > 0 && !(statusFilter != "ALL" && planFilter == "ALL" && dueFilter == "ALL" && expiryFilter == "ALL")) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Filters active (${filteredMembers.size} results)",
              style = MaterialTheme.typography.bodySmall.copy(
                color = GymFlowPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            )

            TextButton(
              onClick = { viewModel.resetMemberFilters() },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset",
                tint = GymFlowDanger,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Reset All",
                color = GymFlowDanger,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // 5. Member Cards List or Empty State
      if (filteredMembers.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = GymFlowSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 24.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(CircleShape)
                  .background(GymFlowSurfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PeopleOutline,
                  contentDescription = null,
                  tint = GymFlowTextSecondary,
                  modifier = Modifier.size(32.dp)
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = if (totalCount == 0) "No members registered yet" else "No members found",
                color = GymFlowTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = if (totalCount == 0)
                  "Start managing your gym by adding your very first member."
                else
                  "Try another name or mobile number.",
                color = GymFlowTextSecondary,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )

              Spacer(modifier = Modifier.height(18.dp))

              if (totalCount == 0) {
                Button(
                  onClick = { viewModel.openSubScreen(SubScreen.ADD_MEMBER) },
                  colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Add First Member", color = Color.White, fontWeight = FontWeight.Bold)
                }
              } else {
                OutlinedButton(
                  onClick = { viewModel.resetMemberFilters() },
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, tint = GymFlowPrimary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Reset Filters", color = GymFlowPrimary, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      } else {
        items(filteredMembers, key = { it.id }) { member ->
          MemberRowCard(
            member = member,
            onClick = { viewModel.openSubScreen(SubScreen.MEMBER_PROFILE, member.id) },
            onCallClick = { viewModel.dialMember(context, member.mobile) },
            onWhatsAppClick = { viewModel.sendWhatsApp(context, member) },
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 5.dp)
              .testTag("member_row_${member.id}")
          )
        }
      }
    }

    // 6. Mobile-First Solid Green "+ Add Member" Button Pinned at Bottom
    Surface(
      color = Color.Transparent,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 76.dp)
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
    ) {
      Button(
        onClick = { viewModel.openSubScreen(SubScreen.ADD_MEMBER) },
        colors = ButtonDefaults.buttonColors(
          containerColor = GymFlowPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("members_add_member_fab")
      ) {
        Icon(
          imageVector = Icons.Default.PersonAdd,
          contentDescription = "Add Member",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "+ Add Member",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 15.sp
          )
        )
      }
    }
  }

  // 7. Filter Bottom Sheet
  if (showFilterBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = { showFilterBottomSheet = false },
      containerColor = GymFlowSurface,
      tonalElevation = 6.dp,
      dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 32.dp)
      ) {
        // Sheet Title & Reset
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Filter Members",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = GymFlowTextPrimary,
              fontSize = 18.sp
            )
          )

          TextButton(
            onClick = {
              viewModel.resetMemberFilters()
              showFilterBottomSheet = false
            }
          ) {
            Text("Reset", color = GymFlowDanger, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Status Filter
        Text(
          text = "STATUS",
          style = MaterialTheme.typography.labelMedium.copy(
            color = GymFlowTextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val statusList = listOf(
            "ALL" to "All",
            "ACTIVE" to "Active",
            "DUE" to "Due",
            "EXPIRING" to "Expiring Soon",
            "EXPIRED" to "Expired",
            "INACTIVE" to "Inactive"
          )
          statusList.forEach { (key, title) ->
            FilterChip(
              selected = statusFilter == key,
              onClick = { viewModel.setMemberStatusFilter(key) },
              label = { Text(title) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GymFlowPrimaryContainer,
                selectedLabelColor = GymFlowPrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Membership Plan Filter
        Text(
          text = "MEMBERSHIP PLAN",
          style = MaterialTheme.typography.labelMedium.copy(
            color = GymFlowTextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = planFilter == "ALL",
            onClick = { viewModel.setMemberPlanFilter("ALL") },
            label = { Text("All Plans") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = GymFlowPrimaryContainer,
              selectedLabelColor = GymFlowPrimary
            )
          )
          plans.filter { it.active }.forEach { plan ->
            FilterChip(
              selected = planFilter == plan.id,
              onClick = { viewModel.setMemberPlanFilter(plan.id) },
              label = { Text(plan.name) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GymFlowPrimaryContainer,
                selectedLabelColor = GymFlowPrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Due Amount Filter
        Text(
          text = "DUE AMOUNT",
          style = MaterialTheme.typography.labelMedium.copy(
            color = GymFlowTextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val dueOptions = listOf(
            "ALL" to "All",
            "NO_DUE" to "No Due (₹0)",
            "HAS_DUE" to "Has Due",
            "HIGH_DUE" to "High Due (> ₹1k)"
          )
          dueOptions.forEach { (key, title) ->
            FilterChip(
              selected = dueFilter == key,
              onClick = { viewModel.setMemberDueFilter(key) },
              label = { Text(title, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GymFlowPrimaryContainer,
                selectedLabelColor = GymFlowPrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Expiry Filter
        Text(
          text = "EXPIRY",
          style = MaterialTheme.typography.labelMedium.copy(
            color = GymFlowTextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val expOptions = listOf(
            "ALL" to "All",
            "EXPIRING_3" to "In 3 Days",
            "EXPIRING_7" to "In 7 Days",
            "EXPIRED" to "Expired"
          )
          expOptions.forEach { (key, title) ->
            FilterChip(
              selected = expiryFilter == key,
              onClick = { viewModel.setMemberExpiryFilter(key) },
              label = { Text(title, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GymFlowPrimaryContainer,
                selectedLabelColor = GymFlowPrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Apply Filters Button
        Button(
          onClick = { showFilterBottomSheet = false },
          colors = ButtonDefaults.buttonColors(containerColor = GymFlowPrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
        ) {
          Text(
            text = "Apply Filters (${filteredMembers.size} Members)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      }
    }
  }
}
