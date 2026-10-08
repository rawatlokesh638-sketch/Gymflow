package com.example.ui.components

import android.content.Context
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Member
import com.example.model.MemberStatus
import com.example.ui.theme.*

@Composable
fun GymFlowTopBar(
  title: String = "GymFlow",
  subtitle: String? = null,
  showBack: Boolean = false,
  onBackClick: () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {}
) {
  Surface(
    color = GymFlowSurface,
    tonalElevation = 1.dp,
    shadowElevation = 1.dp,
    border = androidx.compose.foundation.BorderStroke(1.dp, GymFlowSurfaceBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (showBack) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(GymFlowSurfaceVariant)
            .testTag("top_bar_back_button")
        ) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = GymFlowTextPrimary
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
      } else {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GymFlowPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.FitnessCenter,
            contentDescription = "GymFlow",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = GymFlowTextPrimary,
            fontSize = 17.sp
          )
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
              color = GymFlowTextSecondary,
              fontSize = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      actions()
    }
  }
}

// Aliased for backwards compatibility
@Composable
fun FitProTopBar(
  title: String = "GymFlow",
  subtitle: String? = null,
  showBack: Boolean = false,
  onBackClick: () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {}
) {
  GymFlowTopBar(
    title = title,
    subtitle = subtitle,
    showBack = showBack,
    onBackClick = onBackClick,
    actions = actions
  )
}

@Composable
fun StatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  trend: String? = null,
  isPositiveTrend: Boolean = true,
  icon: ImageVector,
  iconBgColor: Color = GymFlowPrimaryContainer,
  iconTint: Color = GymFlowPrimary,
  valueColor: Color = GymFlowTextPrimary,
  isHighlighted: Boolean = false,
  highlightBorderColor: Color = GymFlowDanger,
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = GymFlowSurface,
    border = androidx.compose.foundation.BorderStroke(
      width = if (isHighlighted) 1.5.dp else 1.dp,
      color = if (isHighlighted) highlightBorderColor else GymFlowSurfaceBorder
    ),
    shadowElevation = if (isHighlighted) 3.dp else 1.dp,
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = iconTint.copy(alpha = 0.15f)),
        onClick = onClick
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(iconBgColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
          )
        }

        if (trend != null) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isPositiveTrend) GymFlowSuccessContainer else GymFlowDangerContainer
          ) {
            Text(
              text = trend,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isPositiveTrend) GymFlowOnSuccessContainer else GymFlowOnDangerContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall.copy(
          color = GymFlowTextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          color = valueColor,
          fontSize = 22.sp
        )
      )

      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall.copy(
            color = if (isHighlighted) highlightBorderColor else GymFlowTextMuted,
            fontSize = 11.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
          )
        )
      }
    }
  }
}

@Composable
fun QuickActionButton(
  title: String,
  icon: ImageVector,
  bgColor: Color = GymFlowPrimary,
  iconColor: Color = Color.White,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = bgColor.copy(alpha = 0.2f)),
        onClick = onClick
      )
      .padding(vertical = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(52.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(bgColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = iconColor,
        modifier = Modifier.size(24.dp)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall.copy(
        color = GymFlowTextPrimary,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        fontSize = 11.sp,
        lineHeight = 14.sp
      ),
      maxLines = 2
    )
  }
}

@Composable
fun InteractiveLaserQrScanner(
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(220.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(Color.White)
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = Icons.Default.QrCode2,
      contentDescription = "QR Code",
      tint = Color.Black,
      modifier = Modifier.size(180.dp)
    )
  }
}

@Composable
fun MemberStatusBadge(
  status: MemberStatus,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, symbol, label) = when (status) {
    MemberStatus.ACTIVE -> Quadruple(GymFlowSuccessContainer, GymFlowOnSuccessContainer, "●", "Active")
    MemberStatus.DUE -> Quadruple(GymFlowDangerContainer, GymFlowOnDangerContainer, "▲", "Due")
    MemberStatus.EXPIRING -> Quadruple(GymFlowWarningContainer, GymFlowOnWarningContainer, "⏱", "Expiring")
    MemberStatus.EXPIRED -> Quadruple(Color(0xFFFFEBEE), Color(0xFFC62828), "✕", "Expired")
    MemberStatus.INACTIVE -> Quadruple(GymFlowSurfaceVariant, GymFlowTextSecondary, "○", "Inactive")
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bgColor,
    modifier = modifier
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Text(
        text = symbol,
        color = textColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun MemberRowCard(
  member: Member,
  onClick: () -> Unit,
  onCallClick: (() -> Unit)? = null,
  onWhatsAppClick: (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = GymFlowSurface,
    border = androidx.compose.foundation.BorderStroke(
      width = if (member.dueAmount > 0) 1.2.dp else 1.dp,
      color = if (member.dueAmount > 0) GymFlowDanger.copy(alpha = 0.35f) else GymFlowSurfaceBorder
    ),
    shadowElevation = 1.dp,
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Top row: Avatar, Name + Mobile, and Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Avatar circle with initials
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
              when (member.status) {
                MemberStatus.DUE -> GymFlowDangerContainer
                MemberStatus.EXPIRING -> GymFlowWarningContainer
                MemberStatus.EXPIRED -> Color(0xFFFFEBEE)
                else -> GymFlowPrimaryContainer
              }
            )
            .border(
              1.5.dp,
              when (member.status) {
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
            text = member.avatarInitial,
            color = when (member.status) {
              MemberStatus.DUE -> GymFlowOnDangerContainer
              MemberStatus.EXPIRING -> GymFlowOnWarningContainer
              MemberStatus.EXPIRED -> Color(0xFFC62828)
              else -> GymFlowPrimary
            },
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = member.name,
              style = MaterialTheme.typography.titleMedium.copy(
                color = GymFlowTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = member.displayCode,
              style = MaterialTheme.typography.labelSmall.copy(
                color = GymFlowTextMuted,
                fontSize = 10.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "📱 ${member.mobile}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = GymFlowTextSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          )
        }

        if (trailingContent != null) {
          trailingContent()
        } else {
          MemberStatusBadge(status = member.status)
        }
      }

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        thickness = 0.8.dp,
        color = GymFlowSurfaceBorder.copy(alpha = 0.6f)
      )

      // Bottom row: Plan Name, Expiry Date, Due Amount, and Quick Contact Icons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = member.planName,
            style = MaterialTheme.typography.bodySmall.copy(
              color = GymFlowTextPrimary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Expires: ${member.expiryDate}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (member.status == MemberStatus.EXPIRED || member.status == MemberStatus.EXPIRING)
                GymFlowWarning
              else
                GymFlowTextMuted,
              fontSize = 11.sp,
              fontWeight = if (member.status == MemberStatus.EXPIRED || member.status == MemberStatus.EXPIRING)
                FontWeight.Bold
              else
                FontWeight.Normal
            )
          )
        }

        // Due amount badge
        Column(
          horizontalAlignment = Alignment.End,
          modifier = Modifier.padding(end = 8.dp)
        ) {
          if (member.dueAmount > 0) {
            Text(
              text = "Due: ₹${member.dueAmount}",
              style = MaterialTheme.typography.bodySmall.copy(
                color = GymFlowDanger,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
              )
            )
          } else {
            Text(
              text = "✓ No Due",
              style = MaterialTheme.typography.bodySmall.copy(
                color = GymFlowSuccess,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            )
          }
        }

        // Quick Call & WhatsApp icon buttons
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (onCallClick != null) {
            IconButton(
              onClick = onCallClick,
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(GymFlowSurfaceVariant)
            ) {
              Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call ${member.name}",
                tint = GymFlowPrimary,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          if (onWhatsAppClick != null) {
            IconButton(
              onClick = onWhatsAppClick,
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(GymFlowPrimaryContainer)
            ) {
              Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "WhatsApp ${member.name}",
                tint = GymFlowPrimary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}
