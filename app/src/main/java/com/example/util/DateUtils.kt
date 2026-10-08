package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

  private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  private val standardFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  private val shortFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

  fun getTodayFormatted(): String {
    return standardFormat.format(Date())
  }

  fun getTodayIso(): String {
    return isoFormat.format(Date())
  }

  fun getTodayLongHeader(): String {
    return SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
  }

  fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
      hour < 12 -> "Good Morning 👋"
      hour < 17 -> "Good Afternoon 👋"
      else -> "Good Evening 👋"
    }
  }

  fun parseDate(dateStr: String): Date? {
    val clean = dateStr.trim()
    val formats = listOf(
      standardFormat,
      isoFormat,
      shortFormat,
      SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
      SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()),
      SimpleDateFormat("d/M/yyyy", Locale.getDefault())
    )
    for (fmt in formats) {
      try {
        val parsed = fmt.parse(clean)
        if (parsed != null) return parsed
      } catch (_: Exception) {}
    }
    return null
  }

  fun formatDate(date: Date): String {
    return standardFormat.format(date)
  }

  fun isToday(dateStr: String): Boolean {
    val d = parseDate(dateStr) ?: return false
    val cal1 = Calendar.getInstance().apply { time = d }
    val cal2 = Calendar.getInstance()
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
      cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
  }

  fun isYesterday(dateStr: String): Boolean {
    val d = parseDate(dateStr) ?: return false
    val cal1 = Calendar.getInstance().apply { time = d }
    val cal2 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
      cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
  }

  fun isExpiringWithin7Days(expiryDateStr: String): Boolean {
    return isExpiringWithinDays(expiryDateStr, 7)
  }

  fun isExpiringWithinDays(expiryDateStr: String, days: Int): Boolean {
    val d = parseDate(expiryDateStr) ?: return false
    val todayCal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val targetCal = Calendar.getInstance().apply {
      time = d
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val diffMillis = targetCal.timeInMillis - todayCal.timeInMillis
    val diffDays = diffMillis / (1000 * 60 * 60 * 24)
    return diffDays in 0..days
  }

  fun isExpired(expiryDateStr: String): Boolean {
    val d = parseDate(expiryDateStr) ?: return false
    val todayCal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val targetCal = Calendar.getInstance().apply {
      time = d
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return targetCal.before(todayCal)
  }

  fun getDaysRemaining(expiryDateStr: String): Int {
    val d = parseDate(expiryDateStr) ?: return 0
    val todayCal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val targetCal = Calendar.getInstance().apply {
      time = d
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val diffMillis = targetCal.timeInMillis - todayCal.timeInMillis
    return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
  }

  fun getDateOffsetDays(days: Int): String {
    val cal = Calendar.getInstance().apply {
      add(Calendar.DAY_OF_YEAR, days)
    }
    return standardFormat.format(cal.time)
  }

  /**
   * Calculates expiry date based on join date and duration days.
   * If inclusiveJoinDay is true (join day is considered Day 1):
   * Expiry date is joinDate + (durationDays - 1) days.
   */
  fun calculateExpiryDate(
    joinDateStr: String,
    durationDays: Int,
    inclusiveJoinDay: Boolean = true
  ): String {
    val baseDate = parseDate(joinDateStr) ?: Date()
    val cal = Calendar.getInstance().apply {
      time = baseDate
      val daysToAdd = if (inclusiveJoinDay) maxOf(0, durationDays - 1) else durationDays
      add(Calendar.DAY_OF_YEAR, daysToAdd)
    }
    return standardFormat.format(cal.time)
  }

  /**
   * Normalizes Indian mobile number to format: +91 XXXXXXXXXX
   */
  fun normalizeIndianMobile(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return when {
      digits.length == 10 -> "+91 $digits"
      digits.length == 12 && digits.startsWith("91") -> "+91 ${digits.substring(2)}"
      digits.length == 11 && digits.startsWith("0") -> "+91 ${digits.substring(1)}"
      else -> raw.trim()
    }
  }

  /**
   * Extracts clean 10-digit number for comparisons
   */
  fun get10DigitMobile(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return when {
      digits.length == 10 -> digits
      digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
      digits.length == 11 && digits.startsWith("0") -> digits.substring(1)
      digits.length > 10 -> digits.takeLast(10)
      else -> digits
    }
  }

  fun isValidIndianMobile(raw: String): Boolean {
    val digits = get10DigitMobile(raw)
    return digits.length == 10 && digits[0] in listOf('6', '7', '8', '9')
  }

  fun cleanPhoneForWhatsApp(mobile: String): String {
    val digits = rawDigits(mobile)
    return when {
      digits.length == 10 -> "91$digits"
      digits.length == 12 && digits.startsWith("91") -> digits
      digits.length == 11 && digits.startsWith("0") -> "91" + digits.substring(1)
      else -> digits
    }
  }

  private fun rawDigits(str: String): String = str.filter { it.isDigit() }
}
