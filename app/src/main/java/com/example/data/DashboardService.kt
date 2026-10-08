package com.example.data

import com.example.model.*
import com.example.util.DateUtils
import java.text.NumberFormat
import java.util.Locale

data class ExpiringMemberItem(
  val memberId: String,
  val name: String,
  val phone: String,
  val photoUrl: String? = null,
  val avatarInitial: String,
  val planName: String,
  val expiryDate: String,
  val daysRemaining: Int,
  val statusText: String
)

data class DueMemberItem(
  val memberId: String,
  val name: String,
  val phone: String,
  val photoUrl: String? = null,
  val avatarInitial: String,
  val dueAmount: Int,
  val daysOverdue: Int,
  val planName: String
)

data class DashboardMetrics(
  val totalMembers: Int = 0,
  val activeMembers: Int = 0,
  val activePercentage: Int = 0,
  val dueMembersCount: Int = 0,
  val totalDueAmount: Int = 0,
  val todayCollectionTotal: Int = 0,
  val todayCollectionCash: Int = 0,
  val todayCollectionUpi: Int = 0,
  val todayCollectionCard: Int = 0,
  val yesterdayCollectionTotal: Int = 0,
  val collectionTrendText: String = "Compared with yesterday",
  val isPositiveTrend: Boolean = true,
  val expiringSoonCount: Int = 0,
  val expiredCount: Int = 0,
  val expiringMembersList: List<ExpiringMemberItem> = emptyList(),
  val topDueMembersList: List<DueMemberItem> = emptyList(),
  val isOffline: Boolean = false,
  val isLoading: Boolean = false,
  val errorMessage: String? = null
) {
  val formattedTotalDue: String
    get() = formatInr(totalDueAmount)

  val formattedTodayTotal: String
    get() = formatInr(todayCollectionTotal)

  val formattedCash: String
    get() = formatInr(todayCollectionCash)

  val formattedUpi: String
    get() = formatInr(todayCollectionUpi)

  val formattedCard: String
    get() = formatInr(todayCollectionCard)

  companion object {
    fun formatInr(amount: Int): String {
      val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
      format.maximumFractionDigits = 0
      return format.format(amount).replace("INR", "₹").trim()
    }
  }
}

class DashboardCalculator {

  fun calculate(
    members: List<Member>,
    payments: List<PaymentRecord>,
    isOffline: Boolean = false,
    isLoading: Boolean = false,
    errorMessage: String? = null
  ): DashboardMetrics {
    // 1. Total Members: all non-deleted registered members
    val nonDeletedMembers = members.filter { !it.deleted }
    val totalMembers = nonDeletedMembers.size

    // 2. Active Members: expiryDate >= today && !deleted && status != INACTIVE
    val activeMembersList = nonDeletedMembers.filter { member ->
      val notExpired = !DateUtils.isExpired(member.expiryDate)
      val notExplicitlyInactive = member.status != MemberStatus.INACTIVE
      notExpired && notExplicitlyInactive
    }
    val activeMembers = activeMembersList.size
    val activePercentage = if (totalMembers > 0) {
      ((activeMembers.toDouble() / totalMembers.toDouble()) * 100).toInt()
    } else 0

    // 3. Due Members: dueAmount > 0 && !deleted
    val dueMembersList = nonDeletedMembers.filter { it.dueAmount > 0 }
    val dueMembersCount = dueMembersList.size
    val totalDueAmount = dueMembersList.sumOf { it.dueAmount }

    // 4. Today's Collection: successful non-deleted payments on today's local date
    val validPayments = payments.filter {
      !it.deleted && it.status == PaymentStatus.SUCCESS
    }
    val todayPayments = validPayments.filter { DateUtils.isToday(it.paymentDate) }
    val todayCollectionTotal = todayPayments.sumOf { it.amount }

    val todayCash = todayPayments.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.amount }
    val todayUpi = todayPayments.filter { it.paymentMethod == PaymentMethod.UPI }.sumOf { it.amount }
    val todayCard = todayPayments.filter { it.paymentMethod == PaymentMethod.CARD }.sumOf { it.amount }

    val yesterdayPayments = validPayments.filter { DateUtils.isYesterday(it.paymentDate) }
    val yesterdayCollectionTotal = yesterdayPayments.sumOf { it.amount }

    val (trendText, isPositiveTrend) = if (yesterdayCollectionTotal > 0) {
      val diff = todayCollectionTotal - yesterdayCollectionTotal
      val percent = ((diff.toDouble() / yesterdayCollectionTotal.toDouble()) * 100).toInt()
      val sign = if (percent >= 0) "+" else ""
      Pair("$sign$percent% vs yesterday", percent >= 0)
    } else {
      Pair("Compared with yesterday", true)
    }

    // 5. Expiring Soon (Within 7 days): >= today && <= today + 7 days
    val expiringSoonList = nonDeletedMembers.filter {
      DateUtils.isExpiringWithin7Days(it.expiryDate)
    }.sortedBy { DateUtils.getDaysRemaining(it.expiryDate) }
    val expiringSoonCount = expiringSoonList.size

    // Expired Members: expiryDate < today
    val expiredCount = nonDeletedMembers.count {
      DateUtils.isExpired(it.expiryDate)
    }

    // Expiring Members formatted items
    val expiringItems = expiringSoonList.map { m ->
      val days = DateUtils.getDaysRemaining(m.expiryDate)
      val statusText = when {
        days == 0 -> "Expires today"
        days == 1 -> "Expires tomorrow"
        days < 0 -> "Expired"
        else -> "Expires in $days days"
      }
      ExpiringMemberItem(
        memberId = m.id,
        name = m.name,
        phone = m.mobile,
        photoUrl = m.photoUrl,
        avatarInitial = m.avatarInitial,
        planName = m.planName,
        expiryDate = m.expiryDate,
        daysRemaining = days,
        statusText = statusText
      )
    }

    // Top 5 Due Members: sorted by highest due amount, then days overdue
    val topDueItems = dueMembersList
      .sortedWith(compareByDescending<Member> { it.dueAmount }.thenByDescending { it.daysOverdue })
      .take(5)
      .map { m ->
        val overdueDays = if (m.daysOverdue > 0) m.daysOverdue else {
          val remaining = DateUtils.getDaysRemaining(m.expiryDate)
          if (remaining < 0) -remaining else 0
        }
        DueMemberItem(
          memberId = m.id,
          name = m.name,
          phone = m.mobile,
          photoUrl = m.photoUrl,
          avatarInitial = m.avatarInitial,
          dueAmount = m.dueAmount,
          daysOverdue = overdueDays,
          planName = m.planName
        )
      }

    return DashboardMetrics(
      totalMembers = totalMembers,
      activeMembers = activeMembers,
      activePercentage = activePercentage,
      dueMembersCount = dueMembersCount,
      totalDueAmount = totalDueAmount,
      todayCollectionTotal = todayCollectionTotal,
      todayCollectionCash = todayCash,
      todayCollectionUpi = todayUpi,
      todayCollectionCard = todayCard,
      yesterdayCollectionTotal = yesterdayCollectionTotal,
      collectionTrendText = trendText,
      isPositiveTrend = isPositiveTrend,
      expiringSoonCount = expiringSoonCount,
      expiredCount = expiredCount,
      expiringMembersList = expiringItems,
      topDueMembersList = topDueItems,
      isOffline = isOffline,
      isLoading = isLoading,
      errorMessage = errorMessage
    )
  }
}
