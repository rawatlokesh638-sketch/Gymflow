package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DashboardCalculator
import com.example.data.DashboardMetrics
import com.example.data.FitProRepository
import com.example.model.*
import com.example.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder

enum class NavigationTab {
  HOME, MEMBERS, ATTENDANCE, PAYMENTS, MORE
}

enum class SubScreen {
  NONE,
  MEMBER_PROFILE,
  ADD_MEMBER,
  EDIT_MEMBER,
  COLLECT_PAYMENT,
  DUE_MEMBERS,
  WHATSAPP_REMINDER,
  ATTENDANCE_LIST,
  REPORTS,
  PLANS_PRICING,
  SUBSCRIPTIONS
}

data class MemberFilterState(
  val status: String = "ALL",
  val planId: String = "ALL",
  val due: String = "ALL",
  val expiry: String = "ALL",
  val query: String = ""
)

class FitProViewModel(
  val repository: FitProRepository = FitProRepository()
) : ViewModel() {

  private val calculator = DashboardCalculator()

  private val _activeTab = MutableStateFlow(NavigationTab.MEMBERS) // Start on Members or Home
  val activeTab: StateFlow<NavigationTab> = _activeTab.asStateFlow()

  private val _activeSubScreen = MutableStateFlow(SubScreen.NONE)
  val activeSubScreen: StateFlow<SubScreen> = _activeSubScreen.asStateFlow()

  private val _selectedMemberId = MutableStateFlow<String?>(null)
  val selectedMemberId: StateFlow<String?> = _selectedMemberId.asStateFlow()

  // --- Member Management Filters ---
  // Status: "ALL", "ACTIVE", "DUE", "EXPIRING", "EXPIRED", "INACTIVE"
  private val _memberFilterStatus = MutableStateFlow("ALL")
  val memberFilterStatus: StateFlow<String> = _memberFilterStatus.asStateFlow()

  // Plan: "ALL" or plan ID
  private val _memberFilterPlanId = MutableStateFlow("ALL")
  val memberFilterPlanId: StateFlow<String> = _memberFilterPlanId.asStateFlow()

  // Due: "ALL", "NO_DUE", "HAS_DUE", "HIGH_DUE"
  private val _memberFilterDue = MutableStateFlow("ALL")
  val memberFilterDue: StateFlow<String> = _memberFilterDue.asStateFlow()

  // Expiry: "ALL", "EXPIRING_3", "EXPIRING_7", "EXPIRED"
  private val _memberFilterExpiry = MutableStateFlow("ALL")
  val memberFilterExpiry: StateFlow<String> = _memberFilterExpiry.asStateFlow()

  // Search Query
  private val _memberSearchQuery = MutableStateFlow("")
  val memberSearchQuery: StateFlow<String> = _memberSearchQuery.asStateFlow()

  // Backwards compatibility
  val memberFilter: StateFlow<String> = _memberFilterStatus

  // Attendance tab & quick check-in
  private val _attendanceTab = MutableStateFlow("QR_CODE")
  val attendanceTab: StateFlow<String> = _attendanceTab.asStateFlow()

  private val _mobileCheckInInput = MutableStateFlow("")
  val mobileCheckInInput: StateFlow<String> = _mobileCheckInInput.asStateFlow()

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  private val _isLoggedIn = MutableStateFlow(true)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  // Offline and Loading States for Dashboard
  private val _isOffline = MutableStateFlow(false)
  val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  // Selected WhatsApp template & recipient filter
  private val _selectedTemplateId = MutableStateFlow("t_due_hi")
  val selectedTemplateId: StateFlow<String> = _selectedTemplateId.asStateFlow()

  private val _whatsAppRecipientCategory = MutableStateFlow("ALL_DUE")
  val whatsAppRecipientCategory: StateFlow<String> = _whatsAppRecipientCategory.asStateFlow()

  // Language: "en" or "hi"
  private val _language = MutableStateFlow("en")
  val language: StateFlow<String> = _language.asStateFlow()

  val members = repository.members
  val attendance = repository.attendance
  val payments = repository.payments
  val plans = repository.plans
  val settings = repository.settings
  val templates = repository.templates
  val appSubscriptions = repository.appSubscriptions

  // Real-time calculated Dashboard metrics
  val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
    members,
    payments,
    _isOffline,
    _isLoading,
    _errorMessage
  ) { mList, pList, offline, loading, err ->
    calculator.calculate(
      members = mList,
      payments = pList,
      isOffline = offline,
      isLoading = loading,
      errorMessage = err
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    DashboardMetrics()
  )

  // Selected member object
  val selectedMember: StateFlow<Member?> = combine(members, _selectedMemberId) { list, id ->
    list.find { it.id == id && !it.deleted } ?: list.firstOrNull { !it.deleted }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Active filters count
  val activeFiltersCount: StateFlow<Int> = combine(
    _memberFilterStatus,
    _memberFilterPlanId,
    _memberFilterDue,
    _memberFilterExpiry
  ) { status, plan, due, exp ->
    var count = 0
    if (status != "ALL") count++
    if (plan != "ALL") count++
    if (due != "ALL") count++
    if (exp != "ALL") count++
    count
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  // Combined filters flow
  private val filterState: StateFlow<MemberFilterState> = combine(
    _memberFilterStatus,
    _memberFilterPlanId,
    _memberFilterDue,
    _memberFilterExpiry,
    _memberSearchQuery
  ) { status, plan, due, exp, query ->
    MemberFilterState(
      status = status,
      planId = plan,
      due = due,
      expiry = exp,
      query = query
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    MemberFilterState()
  )

  // Filtered members list
  val filteredMembers: StateFlow<List<Member>> = combine(
    members,
    filterState
  ) { list, filters ->
    val cleanQuery = filters.query.trim().lowercase()
    val cleanDigits = cleanQuery.filter { it.isDigit() }

    list.filter { member ->
      if (member.deleted) return@filter false

      // 1. Status Filter
      val matchesStatus = when (filters.status) {
        "ALL" -> true
        "ACTIVE" -> member.status == MemberStatus.ACTIVE
        "DUE" -> member.status == MemberStatus.DUE || member.dueAmount > 0
        "EXPIRING" -> member.status == MemberStatus.EXPIRING || DateUtils.isExpiringWithin7Days(member.expiryDate)
        "EXPIRED" -> member.status == MemberStatus.EXPIRED || DateUtils.isExpired(member.expiryDate)
        "INACTIVE" -> member.status == MemberStatus.INACTIVE
        else -> true
      }

      // 2. Plan Filter
      val matchesPlan = when (filters.planId) {
        "ALL" -> true
        else -> member.planId == filters.planId || member.planName.equals(filters.planId, ignoreCase = true)
      }

      // 3. Due Amount Filter
      val matchesDue = when (filters.due) {
        "ALL" -> true
        "NO_DUE" -> member.dueAmount == 0
        "HAS_DUE" -> member.dueAmount > 0
        "HIGH_DUE" -> member.dueAmount > 1000
        else -> true
      }

      // 4. Expiry Filter
      val matchesExpiry = when (filters.expiry) {
        "ALL" -> true
        "EXPIRING_3" -> DateUtils.isExpiringWithinDays(member.expiryDate, 3) && !DateUtils.isExpired(member.expiryDate)
        "EXPIRING_7" -> DateUtils.isExpiringWithinDays(member.expiryDate, 7) && !DateUtils.isExpired(member.expiryDate)
        "EXPIRED" -> DateUtils.isExpired(member.expiryDate)
        else -> true
      }

      // 5. Search Query (Name, Phone, Member ID, Plan)
      val matchesSearch = if (cleanQuery.isBlank()) {
        true
      } else {
        member.name.lowercase().contains(cleanQuery) ||
          member.displayCode.lowercase().contains(cleanQuery) ||
          member.id.lowercase().contains(cleanQuery) ||
          member.planName.lowercase().contains(cleanQuery) ||
          (cleanDigits.isNotEmpty() && member.mobile.filter { it.isDigit() }.contains(cleanDigits))
      }

      matchesStatus && matchesPlan && matchesDue && matchesExpiry && matchesSearch
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setTab(tab: NavigationTab) {
    _activeTab.value = tab
    _activeSubScreen.value = SubScreen.NONE
  }

  fun openSubScreen(screen: SubScreen, memberId: String? = null) {
    if (memberId != null) {
      _selectedMemberId.value = memberId
    }
    _activeSubScreen.value = screen
  }

  fun closeSubScreen() {
    _activeSubScreen.value = SubScreen.NONE
  }

  fun setMemberStatusFilter(status: String) {
    _memberFilterStatus.value = status
  }

  fun setMemberPlanFilter(planId: String) {
    _memberFilterPlanId.value = planId
  }

  fun setMemberDueFilter(due: String) {
    _memberFilterDue.value = due
  }

  fun setMemberExpiryFilter(expiry: String) {
    _memberFilterExpiry.value = expiry
  }

  fun resetMemberFilters() {
    _memberFilterStatus.value = "ALL"
    _memberFilterPlanId.value = "ALL"
    _memberFilterDue.value = "ALL"
    _memberFilterExpiry.value = "ALL"
    _memberSearchQuery.value = ""
  }

  fun setMemberFilter(filter: String) {
    _memberFilterStatus.value = filter
  }

  fun setMemberSearchQuery(query: String) {
    _memberSearchQuery.value = query
  }

  fun checkDuplicateMobile(mobile: String, excludeMemberId: String? = null): Member? {
    val existing = repository.findMemberByMobile(mobile) ?: return null
    return if (excludeMemberId != null && existing.id == excludeMemberId) null else existing
  }

  fun addMember(
    name: String,
    phone: String,
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
    paymentType: PaymentMethod = PaymentMethod.UPI,
    notes: String = ""
  ): Member {
    val newMember = repository.addMember(
      name = name,
      mobile = phone,
      photoUrl = photoUrl,
      dob = dob,
      gender = gender,
      address = address,
      emergencyContact = emergencyContact,
      planId = planId,
      planName = planName,
      joinDate = joinDate,
      expiryDate = expiryDate,
      feesAmount = feesAmount,
      paidAmount = paidAmount,
      paymentMethod = paymentType,
      notes = notes
    )
    showSnackbar("✓ Member ${newMember.name} registered successfully!")
    _selectedMemberId.value = newMember.id
    closeSubScreen()
    return newMember
  }

  fun updateMember(updated: Member) {
    val result = repository.updateMember(updated)
    _selectedMemberId.value = result.id
    showSnackbar("✓ Member ${result.name} updated successfully")
    closeSubScreen()
  }

  fun changeMemberPlan(
    memberId: String,
    planId: String,
    planName: String,
    newExpiryDate: String,
    newFees: Int
  ) {
    val updated = repository.changeMemberPlan(
      memberId = memberId,
      newPlanId = planId,
      newPlanName = planName,
      newExpiryDate = newExpiryDate,
      newFees = newFees
    )
    if (updated != null) {
      showSnackbar("✓ Membership plan changed to $planName")
    }
  }

  fun deactivateMember(memberId: String) {
    val member = members.value.find { it.id == memberId }
    repository.deactivateMember(memberId)
    showSnackbar("${member?.name ?: "Member"} marked as Inactive")
  }

  fun reactivateMember(memberId: String) {
    val member = members.value.find { it.id == memberId }
    repository.reactivateMember(memberId)
    showSnackbar("${member?.name ?: "Member"} reactivated")
  }

  fun deleteMember(memberId: String) {
    val member = members.value.find { it.id == memberId }
    repository.deleteMember(memberId)
    showSnackbar("Member ${member?.name ?: ""} removed from records")
    closeSubScreen()
  }

  fun collectPayment(
    memberId: String,
    amount: Int,
    paymentType: PaymentMethod = PaymentMethod.UPI,
    notes: String = "Fee collection"
  ) {
    if (amount <= 0) {
      showSnackbar("Please enter a valid amount")
      return
    }
    val record = repository.recordPayment(memberId, amount, paymentType, notes)
    showSnackbar("✓ Payment of ₹$amount received (${record.paymentType.name})")
    if (_activeSubScreen.value == SubScreen.COLLECT_PAYMENT) {
      closeSubScreen()
    }
  }

  fun dialMember(context: Context, mobile: String) {
    try {
      val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile"))
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    } catch (_: Exception) {
      showSnackbar("Cannot open dialer for $mobile")
    }
  }

  fun sendWhatsApp(context: Context, member: Member, customMessage: String? = null) {
    val currentSettings = repository.settings.value
    val msg = customMessage ?: if (member.dueAmount > 0) {
      "नमस्ते ${member.name} जी, आपकी जिम की फीस ₹${member.dueAmount} अभी बाकी है। कृपया जल्द से जल्द पेमेंट करें ताकि आपकी मेंबरशिप जारी रहे। - ${currentSettings.gymName}"
    } else {
      "नमस्ते ${member.name} जी, आपकी मेंबरशिप ${member.expiryDate} तक एक्टिव है। - ${currentSettings.gymName}"
    }

    try {
      val encoded = URLEncoder.encode(msg, "UTF-8")
      val cleanPhone = DateUtils.cleanPhoneForWhatsApp(member.mobile)
      val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded")
      val intent = Intent(Intent.ACTION_VIEW, uri)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
      showSnackbar("Opening WhatsApp for ${member.name}...")
    } catch (_: Exception) {
      showSnackbar("WhatsApp is not installed or available on this device")
    }
  }

  fun sendReminder(context: Context, memberId: String) {
    val member = members.value.find { it.id == memberId } ?: return
    val reminderText = "नमस्ते ${member.name} जी, आपकी जिम की फीस ₹${member.dueAmount} अभी बाकी है। कृपया जल्द से जल्द पेमेंट करें ताकि आपकी मेंबरशिप जारी रहे। - ${settings.value.gymName}"
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("GymFlow Reminder", reminderText)
    clipboard?.setPrimaryClip(clip)
    showSnackbar("Reminder copied to clipboard for ${member.name}")
  }

  fun sendSMS(context: Context, member: Member, message: String) {
    try {
      val smsUri = Uri.parse("sms:${member.mobile}?body=" + Uri.encode(message))
      val intent = Intent(Intent.ACTION_VIEW, smsUri)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    } catch (_: Exception) {
      Toast.makeText(context, "Cannot launch SMS app", Toast.LENGTH_SHORT).show()
    }
  }

  fun setAttendanceTab(tab: String) {
    _attendanceTab.value = tab
  }

  fun setMobileCheckInInput(text: String) {
    _mobileCheckInInput.value = text
  }

  fun setSelectedTemplate(templateId: String) {
    _selectedTemplateId.value = templateId
  }

  fun setWhatsAppRecipientCategory(category: String) {
    _whatsAppRecipientCategory.value = category
  }

  fun toggleLanguage() {
    _language.value = if (_language.value == "en") "hi" else "en"
    val gymSettings = repository.settings.value
    repository.updateGymSettings(gymSettings.copy(language = _language.value))
    showSnackbar(if (_language.value == "hi") "भाषा बदलकर हिंदी की गई" else "Language switched to English")
  }

  fun showSnackbar(msg: String) {
    _snackbarMessage.value = msg
  }

  fun clearSnackbar() {
    _snackbarMessage.value = null
  }

  fun refreshDashboard() {
    viewModelScope.launch {
      _isLoading.value = true
      _errorMessage.value = null
      delay(300)
      _isLoading.value = false
      showSnackbar("✓ Data refreshed & metrics synced")
    }
  }

  fun retryDashboard() {
    viewModelScope.launch {
      _errorMessage.value = null
      _isLoading.value = true
      delay(300)
      _isLoading.value = false
      showSnackbar("✓ Reloaded successfully")
    }
  }

  fun toggleOffline() {
    val current = _isOffline.value
    _isOffline.value = !current
    if (!current) {
      showSnackbar("Offline mode: Displaying locally cached data")
    } else {
      showSnackbar("Back online: Synced live gym records")
      refreshDashboard()
    }
  }

  fun seedSampleData() {
    repository.seedRealisticData()
    showSnackbar("✓ Loaded PowerFit Gym sample records")
  }

  fun resetAllDataToZero() {
    repository.resetToZero()
    _selectedMemberId.value = null
    showSnackbar("✓ Clean state: All metrics set to 0")
  }

  fun login() {
    _isLoggedIn.value = true
    showSnackbar("Welcome to GymFlow!")
  }

  fun logout() {
    _isLoggedIn.value = false
  }

  fun checkInMember(memberId: String, method: AttendanceMethod = AttendanceMethod.QR_CODE) {
    val record = repository.checkInMember(memberId, method)
    showSnackbar("✓ ${record.memberName} checked in (${record.checkInTime})")
  }

  fun submitMobileCheckIn() {
    val query = _mobileCheckInInput.value.trim()
    if (query.length < 4) {
      showSnackbar("Please enter a valid mobile number")
      return
    }
    val record = repository.checkInByPhone(query)
    if (record != null) {
      showSnackbar("✓ ${record.memberName} checked in successfully! (Plan: ${record.planName})")
      _mobileCheckInInput.value = ""
    } else {
      showSnackbar("No active member found with mobile $query")
    }
  }

  fun updatePlanPrice(planId: String, newPrice: Int) {
    repository.updatePlanPrice(planId, newPrice)
    showSnackbar("✓ Plan pricing updated to ₹$newPrice")
  }

  fun toggleOfflineBackup() {
    val current = repository.settings.value
    repository.updateGymSettings(current.copy(isOfflineBackupEnabled = !current.isOfflineBackupEnabled))
    showSnackbar(if (!current.isOfflineBackupEnabled) "Offline Backup enabled" else "Offline Backup disabled")
  }
}
