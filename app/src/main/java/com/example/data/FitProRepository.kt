package com.example.data

import com.example.model.*
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FitProRepository {

  private val _members = MutableStateFlow<List<Member>>(emptyList())
  val members: StateFlow<List<Member>> = _members.asStateFlow()

  private val _attendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
  val attendance: StateFlow<List<AttendanceRecord>> = _attendance.asStateFlow()

  private val _payments = MutableStateFlow<List<PaymentRecord>>(emptyList())
  val payments: StateFlow<List<PaymentRecord>> = _payments.asStateFlow()

  private val _plans = MutableStateFlow<List<MembershipPlan>>(emptyList())
  val plans: StateFlow<List<MembershipPlan>> = _plans.asStateFlow()

  private val _settings = MutableStateFlow(GymSettings())
  val settings: StateFlow<GymSettings> = _settings.asStateFlow()

  private val _templates = MutableStateFlow<List<WhatsAppTemplate>>(emptyList())
  val templates: StateFlow<List<WhatsAppTemplate>> = _templates.asStateFlow()

  private val _appSubscriptions = MutableStateFlow<List<AppSubscriptionPlan>>(emptyList())
  val appSubscriptions: StateFlow<List<AppSubscriptionPlan>> = _appSubscriptions.asStateFlow()

  init {
    loadInitialData()
  }

  private fun loadInitialData() {
    _plans.value = listOf(
      MembershipPlan(
        id = "plan_student",
        name = "Student Plan",
        durationMonths = 1,
        durationDays = 30,
        price = 499,
        description = "Full gym access for college & school students.",
        badge = "Affordable",
        active = true
      ),
      MembershipPlan(
        id = "plan_female",
        name = "Female Plan",
        durationMonths = 1,
        durationDays = 30,
        price = 699,
        description = "Dedicated female trainer hours & cardio access.",
        badge = "Special",
        active = true
      ),
      MembershipPlan(
        id = "plan_general",
        name = "General Plan",
        durationMonths = 1,
        durationDays = 30,
        price = 799,
        description = "Weights, cardio, locker and free guidance.",
        badge = "Popular",
        active = true
      ),
      MembershipPlan(
        id = "plan_3m",
        name = "3 Month Power",
        durationMonths = 3,
        durationDays = 90,
        price = 1899,
        description = "Quarterly plan with 20% discount.",
        badge = "Save 20%",
        active = true
      ),
      MembershipPlan(
        id = "plan_6m",
        name = "6 Month Plan",
        durationMonths = 6,
        durationDays = 180,
        price = 3299,
        description = "Biannual fitness pass.",
        badge = "Value",
        active = true
      ),
      MembershipPlan(
        id = "plan_1y",
        name = "1 Year Plan",
        durationMonths = 12,
        durationDays = 365,
        price = 5499,
        description = "Annual membership. Just ₹458/mo.",
        badge = "Best Value",
        active = true
      )
    )

    // Load initial realistic SaaS data for Dharuhera gym
    seedRealisticData()

    _templates.value = listOf(
      WhatsAppTemplate(
        id = "t_due_hi",
        title = "Fees Reminder (Hindi)",
        category = TemplateCategory.DUE_REMINDER,
        content = "नमस्ते {name} जी, आपकी जिम की फीस ₹{amount} अभी बाकी है। कृपया जल्द से जल्द पेमेंट करें ताकि आपकी मेंबरशिप जारी रहे। धन्यवाद 🙏 - {gym_name}",
        language = "hi"
      ),
      WhatsAppTemplate(
        id = "t_due_en",
        title = "Fees Reminder (English)",
        category = TemplateCategory.DUE_REMINDER,
        content = "Hello {name}, your gym fee of ₹{amount} is pending. Kindly pay at the reception or UPI to maintain uninterrupted workouts. Thank you! - {gym_name}",
        language = "en"
      ),
      WhatsAppTemplate(
        id = "t_exp_hi",
        title = "Expiry Alert (Hindi)",
        category = TemplateCategory.EXPIRY,
        content = "प्रिय {name}, आपकी {plan} मेंबरशिप {expiry} को समाप्त हो रही है। बिना किसी ब्रेक के अपनी फिटनेस जारी रखने के लिए आज ही रिन्यू करें! - {gym_name}",
        language = "hi"
      )
    )
  }

  fun seedRealisticData() {
    val (mList, pList, aList) = GymFlowDataSeeder.generateRealisticGymData()
    _members.value = mList
    _payments.value = pList
    _attendance.value = aList
  }

  fun resetToZero() {
    _members.value = emptyList()
    _attendance.value = emptyList()
    _payments.value = emptyList()
  }

  fun findMemberByMobile(mobile: String): Member? {
    val clean10 = DateUtils.get10DigitMobile(mobile)
    if (clean10.length < 5) return null
    return _members.value.firstOrNull {
      !it.deleted && DateUtils.get10DigitMobile(it.mobile) == clean10
    }
  }

  fun findMemberById(id: String): Member? {
    return _members.value.firstOrNull { it.id == id && !it.deleted }
  }

  fun addMember(
    name: String,
    mobile: String,
    photoUrl: String? = null,
    dob: String = "15 Jan 1998",
    gender: String = "Male",
    address: String = "",
    emergencyContact: String = "",
    planId: String = "plan_general",
    planName: String = "General Plan",
    joinDate: String = DateUtils.getTodayFormatted(),
    expiryDate: String = DateUtils.getDateOffsetDays(30),
    feesAmount: Int = 799,
    paidAmount: Int = feesAmount,
    paymentMethod: PaymentMethod = PaymentMethod.UPI,
    notes: String = ""
  ): Member {
    val id = "m_" + UUID.randomUUID().toString().take(8)
    val normMobile = DateUtils.normalizeIndianMobile(mobile)
    val dueAmt = maxOf(0, feesAmount - paidAmount)

    // Determine status
    val status = when {
      DateUtils.isExpired(expiryDate) -> MemberStatus.EXPIRED
      DateUtils.isExpiringWithin7Days(expiryDate) -> MemberStatus.EXPIRING
      dueAmt > 0 -> MemberStatus.DUE
      else -> MemberStatus.ACTIVE
    }

    val initials = name.trim().split(" ")
      .filter { it.isNotBlank() }
      .take(2)
      .mapNotNull { it.firstOrNull()?.uppercaseChar() }
      .joinToString("")
      .ifBlank { name.take(2).uppercase() }

    val nextCodeNumber = (_members.value.size + 1).toString().padStart(4, '0')
    val memberCode = "#GF-$nextCodeNumber"

    val newMember = Member(
      id = id,
      name = name.trim(),
      mobile = normMobile,
      photoUrl = photoUrl,
      planId = planId,
      planName = planName,
      joinDate = joinDate,
      expiryDate = expiryDate,
      totalFees = feesAmount,
      paidAmount = paidAmount,
      dueAmount = dueAmt,
      status = status,
      deleted = false,
      createdAt = System.currentTimeMillis(),
      avatarInitial = initials,
      notes = notes.ifBlank { "Registered via GymFlow" },
      daysOverdue = if (dueAmt > 0) 1 else 0,
      dob = dob,
      gender = gender,
      address = address,
      emergencyContact = emergencyContact,
      memberCode = memberCode
    )

    _members.value = listOf(newMember) + _members.value

    if (paidAmount > 0) {
      val payRecord = PaymentRecord(
        id = "pay_" + UUID.randomUUID().toString().take(8),
        memberId = id,
        memberName = name.trim(),
        planName = planName,
        amount = paidAmount,
        paymentMethod = paymentMethod,
        paymentDate = joinDate,
        status = PaymentStatus.SUCCESS,
        notes = "Registration fee payment",
        invoiceNo = "INV-" + (8900 + (1..99).random())
      )
      _payments.value = listOf(payRecord) + _payments.value
    }

    return newMember
  }

  fun updateMember(updated: Member): Member {
    val dueAmt = maxOf(0, updated.totalFees - updated.paidAmount)
    val computedStatus = if (updated.status == MemberStatus.INACTIVE) {
      MemberStatus.INACTIVE
    } else when {
      DateUtils.isExpired(updated.expiryDate) -> MemberStatus.EXPIRED
      DateUtils.isExpiringWithin7Days(updated.expiryDate) -> MemberStatus.EXPIRING
      dueAmt > 0 -> MemberStatus.DUE
      else -> MemberStatus.ACTIVE
    }

    val initials = updated.name.trim().split(" ")
      .filter { it.isNotBlank() }
      .take(2)
      .mapNotNull { it.firstOrNull()?.uppercaseChar() }
      .joinToString("")
      .ifBlank { updated.name.take(2).uppercase() }

    val finalized = updated.copy(
      dueAmount = dueAmt,
      status = computedStatus,
      avatarInitial = initials,
      mobile = DateUtils.normalizeIndianMobile(updated.mobile)
    )

    _members.value = _members.value.map { if (it.id == finalized.id) finalized else it }
    return finalized
  }

  fun deactivateMember(memberId: String) {
    _members.value = _members.value.map {
      if (it.id == memberId) it.copy(status = MemberStatus.INACTIVE) else it
    }
  }

  fun reactivateMember(memberId: String) {
    _members.value = _members.value.map {
      if (it.id == memberId) {
        val due = it.dueAmount
        val status = if (due > 0) MemberStatus.DUE else MemberStatus.ACTIVE
        it.copy(status = status)
      } else it
    }
  }

  fun deleteMember(memberId: String) {
    _members.value = _members.value.map {
      if (it.id == memberId) it.copy(deleted = true) else it
    }
  }

  fun changeMemberPlan(
    memberId: String,
    newPlanId: String,
    newPlanName: String,
    newExpiryDate: String,
    newFees: Int
  ): Member? {
    val member = _members.value.firstOrNull { it.id == memberId } ?: return null
    val newTotalFees = newFees
    val newDue = maxOf(0, newTotalFees - member.paidAmount)
    val newStatus = when {
      DateUtils.isExpired(newExpiryDate) -> MemberStatus.EXPIRED
      DateUtils.isExpiringWithin7Days(newExpiryDate) -> MemberStatus.EXPIRING
      newDue > 0 -> MemberStatus.DUE
      else -> MemberStatus.ACTIVE
    }

    val updated = member.copy(
      planId = newPlanId,
      planName = newPlanName,
      expiryDate = newExpiryDate,
      totalFees = newTotalFees,
      dueAmount = newDue,
      status = newStatus
    )
    updateMember(updated)
    return updated
  }

  fun recordPayment(
    memberId: String,
    amount: Int,
    paymentMethod: PaymentMethod,
    notes: String
  ): PaymentRecord {
    val member = _members.value.find { it.id == memberId }
    val today = DateUtils.getTodayFormatted()
    val record = PaymentRecord(
      id = "pay_" + UUID.randomUUID().toString().take(8),
      memberId = memberId,
      memberName = member?.name ?: "Member",
      planName = member?.planName ?: "General Plan",
      amount = amount,
      paymentMethod = paymentMethod,
      paymentDate = today,
      status = PaymentStatus.SUCCESS,
      notes = notes,
      invoiceNo = "INV-" + (9000 + (1..99).random())
    )
    _payments.value = listOf(record) + _payments.value

    if (member != null) {
      val newPaid = member.paidAmount + amount
      val newDue = maxOf(0, member.totalFees - newPaid)
      val newStatus = when {
        member.status == MemberStatus.INACTIVE -> MemberStatus.INACTIVE
        DateUtils.isExpired(member.expiryDate) -> MemberStatus.EXPIRED
        DateUtils.isExpiringWithin7Days(member.expiryDate) -> MemberStatus.EXPIRING
        newDue > 0 -> MemberStatus.DUE
        else -> MemberStatus.ACTIVE
      }
      updateMember(member.copy(paidAmount = newPaid, dueAmount = newDue, status = newStatus))
    }
    return record
  }

  fun checkInMember(memberId: String, method: AttendanceMethod): AttendanceRecord {
    val member = _members.value.find { it.id == memberId }
    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    val today = DateUtils.getTodayFormatted()
    val record = AttendanceRecord(
      id = "att_" + UUID.randomUUID().toString().take(8),
      memberId = memberId,
      memberName = member?.name ?: "Member",
      planName = member?.planName ?: "General Plan",
      checkInTime = time,
      date = today,
      status = AttendanceStatus.PRESENT,
      method = method
    )
    _attendance.value = listOf(record) + _attendance.value
    return record
  }

  fun checkInByPhone(phoneQuery: String): AttendanceRecord? {
    val clean = phoneQuery.filter { it.isDigit() }
    val member = _members.value.find { it.mobile.filter { ch -> ch.isDigit() }.contains(clean) }
    return if (member != null) {
      checkInMember(member.id, AttendanceMethod.MANUAL)
    } else null
  }

  fun updatePlanPrice(planId: String, newPrice: Int) {
    _plans.value = _plans.value.map {
      if (it.id == planId) it.copy(price = newPrice) else it
    }
  }

  fun updateGymSettings(newSettings: GymSettings) {
    _settings.value = newSettings
  }
}
