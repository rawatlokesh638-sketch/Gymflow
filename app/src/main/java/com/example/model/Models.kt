package com.example.model

enum class MemberStatus {
  ACTIVE, DUE, EXPIRING, EXPIRED, INACTIVE
}

enum class PaymentStatus {
  SUCCESS, PENDING, FAILED, CANCELLED
}

enum class AttendanceStatus {
  PRESENT, ABSENT
}

enum class AttendanceMethod {
  QR_CODE, MANUAL, OFFLINE
}

enum class PaymentMethod {
  CASH, UPI, CARD
}

enum class TemplateCategory {
  DUE_REMINDER, EXPIRY, PAYMENT, BIRTHDAY
}

data class Member(
  val id: String,
  val name: String,
  val mobile: String,
  val photoUrl: String? = null,
  val planId: String = "plan_general",
  val planName: String = "General Plan",
  val joinDate: String,
  val expiryDate: String,
  val totalFees: Int,
  val paidAmount: Int,
  val dueAmount: Int,
  val status: MemberStatus = MemberStatus.ACTIVE,
  val deleted: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val avatarInitial: String = name.take(2).uppercase(),
  val notes: String = "",
  val daysOverdue: Int = 0,
  val dob: String = "15 Jan 1998",
  val gender: String = "Male",
  val address: String = "",
  val emergencyContact: String = "",
  val memberCode: String = ""
) {
  // Backwards compatibility property
  val phone: String get() = mobile

  // Display code e.g. #GF-1048
  val displayCode: String
    get() = if (memberCode.isNotBlank()) memberCode else "#M-${id.takeLast(4).uppercase()}"
}

data class PaymentRecord(
  val id: String,
  val memberId: String,
  val amount: Int,
  val paymentMethod: PaymentMethod = PaymentMethod.UPI,
  val paymentDate: String,
  val status: PaymentStatus = PaymentStatus.SUCCESS,
  val deleted: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val memberName: String = "",
  val planName: String = "",
  val notes: String = "",
  val invoiceNo: String = ""
) {
  // Backwards compatibility properties
  val paymentType: PaymentMethod get() = paymentMethod
  val date: String get() = paymentDate
}

data class AttendanceRecord(
  val id: String,
  val memberId: String,
  val memberName: String,
  val planName: String,
  val checkInTime: String,
  val date: String,
  val status: AttendanceStatus = AttendanceStatus.PRESENT,
  val method: AttendanceMethod = AttendanceMethod.QR_CODE
)

data class MembershipPlan(
  val id: String,
  val name: String,
  val durationMonths: Int = 1,
  val price: Int,
  val description: String = "",
  val badge: String? = null,
  val durationDays: Int = durationMonths * 30,
  val active: Boolean = true
)

data class GymSettings(
  val gymName: String = "PowerFit Gym",
  val gymLocation: String = "Dharuhera, Haryana",
  val brandName: String = "GymFlow",
  val ownerPhone: String = "+91 9876543210",
  val upiId: String = "powerfitgym@okaxis",
  val isOfflineBackupEnabled: Boolean = true,
  val language: String = "en", // "en" or "hi"
  val activeSubscription: String = "Pro Plan",
  val subscriptionExpiry: String = "12 Dec 2027"
)

data class WhatsAppTemplate(
  val id: String,
  val title: String,
  val category: TemplateCategory,
  val content: String,
  val language: String = "hi"
)

data class AppSubscriptionPlan(
  val id: String,
  val name: String,
  val pricePerMonth: Int,
  val billingCycle: String,
  val totalCost: Int,
  val discountPercent: Int,
  val features: List<String>,
  val isPopular: Boolean = false
)
