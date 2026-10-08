package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DashboardCalculator
import com.example.data.FitProRepository
import com.example.model.*
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `verify app name matches GymFlow`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GymFlow", appName)
  }

  @Test
  fun `verify dashboard metrics calculations for realistic seeded data`() {
    val repository = FitProRepository()
    repository.seedRealisticData()

    val calculator = DashboardCalculator()
    val metrics = calculator.calculate(
      members = repository.members.value,
      payments = repository.payments.value
    )

    // 1. Total Members (186 registered members)
    assertEquals(186, metrics.totalMembers)

    // 2. Active Members (162 active, 87% percentage)
    assertEquals(162, metrics.activeMembers)
    assertEquals(87, metrics.activePercentage)

    // 3. Due Members (24 due members, total outstanding ₹18,900)
    assertEquals(24, metrics.dueMembersCount)
    assertEquals(18900, metrics.totalDueAmount)

    // 4. Today's Collection (Total: ₹8,450; Cash: ₹2,500; UPI: ₹5,450; Card: ₹500)
    assertEquals(8450, metrics.todayCollectionTotal)
    assertEquals(2500, metrics.todayCollectionCash)
    assertEquals(5450, metrics.todayCollectionUpi)
    assertEquals(500, metrics.todayCollectionCard)
    assertEquals("+12% vs yesterday", metrics.collectionTrendText)
    assertTrue(metrics.isPositiveTrend)

    // 5. Expiring Soon (12 members expiring within 7 days)
    assertEquals(12, metrics.expiringSoonCount)
  }

  @Test
  fun `verify zero state when all data is reset`() {
    val repository = FitProRepository()
    repository.resetToZero()

    val calculator = DashboardCalculator()
    val metrics = calculator.calculate(
      members = repository.members.value,
      payments = repository.payments.value
    )

    assertEquals(0, metrics.totalMembers)
    assertEquals(0, metrics.activeMembers)
    assertEquals(0, metrics.activePercentage)
    assertEquals(0, metrics.dueMembersCount)
    assertEquals(0, metrics.totalDueAmount)
    assertEquals(0, metrics.todayCollectionTotal)
    assertEquals(0, metrics.todayCollectionCash)
    assertEquals(0, metrics.todayCollectionUpi)
    assertEquals(0, metrics.todayCollectionCard)
    assertEquals(0, metrics.expiringSoonCount)
    assertTrue(metrics.expiringMembersList.isEmpty())
    assertTrue(metrics.topDueMembersList.isEmpty())
  }

  @Test
  fun `verify adding member and recording payment dynamically updates calculations`() {
    val repository = FitProRepository()
    repository.resetToZero()

    // Add member with partial payment (dueAmount = 500)
    val member = repository.addMember(
      name = "Amit Kumar",
      mobile = "9876543210",
      feesAmount = 1500,
      paidAmount = 1000,
      paymentMethod = PaymentMethod.UPI
    )

    val calculator = DashboardCalculator()
    var metrics = calculator.calculate(
      members = repository.members.value,
      payments = repository.payments.value
    )

    assertEquals(1, metrics.totalMembers)
    assertEquals(1, metrics.dueMembersCount)
    assertEquals(500, metrics.totalDueAmount)
    assertEquals(1000, metrics.todayCollectionTotal)
    assertEquals(1000, metrics.todayCollectionUpi)

    // Now record remaining payment
    repository.recordPayment(
      memberId = member.id,
      amount = 500,
      paymentMethod = PaymentMethod.CASH,
      notes = "Remaining balance"
    )

    metrics = calculator.calculate(
      members = repository.members.value,
      payments = repository.payments.value
    )

    assertEquals(1, metrics.totalMembers)
    assertEquals(0, metrics.dueMembersCount)
    assertEquals(0, metrics.totalDueAmount)
    assertEquals(1500, metrics.todayCollectionTotal)
    assertEquals(500, metrics.todayCollectionCash)
    assertEquals(1000, metrics.todayCollectionUpi)
  }
}
