package com.example.data

import com.example.model.*
import com.example.util.DateUtils
import java.util.UUID

object GymFlowDataSeeder {

  fun generateRealisticGymData(): Triple<List<Member>, List<PaymentRecord>, List<AttendanceRecord>> {
    val today = DateUtils.getTodayFormatted()
    val yesterday = DateUtils.getDateOffsetDays(-1)

    // Key named members for previews & reminders in Dharuhera / Rewari
    val highlightedMembers = listOf(
      // Top Due Members
      Member(
        id = "m_aman_01",
        name = "Aman Kumar",
        mobile = "+91 98123 45678",
        planName = "General Plan",
        joinDate = DateUtils.getDateOffsetDays(-45),
        expiryDate = DateUtils.getDateOffsetDays(-7),
        totalFees = 1400,
        paidAmount = 700,
        dueAmount = 700,
        status = MemberStatus.DUE,
        avatarInitial = "AK",
        daysOverdue = 7,
        notes = "Follow up regarding evening slot renewal"
      ),
      Member(
        id = "m_vikas_02",
        name = "Vikas Yadav",
        mobile = "+91 98234 56789",
        planName = "3 Month Power",
        joinDate = DateUtils.getDateOffsetDays(-102),
        expiryDate = DateUtils.getDateOffsetDays(-12),
        totalFees = 1899,
        paidAmount = 399,
        dueAmount = 1500,
        status = MemberStatus.DUE,
        avatarInitial = "VY",
        daysOverdue = 12,
        notes = "Promised cash payment by Friday"
      ),
      Member(
        id = "m_rahul_03",
        name = "Rahul Verma",
        mobile = "+91 98345 67890",
        planName = "General Plan",
        joinDate = DateUtils.getDateOffsetDays(-69),
        expiryDate = DateUtils.getDateOffsetDays(-9),
        totalFees = 1400,
        paidAmount = 200,
        dueAmount = 1200,
        status = MemberStatus.DUE,
        avatarInitial = "RV",
        daysOverdue = 9
      ),
      Member(
        id = "m_amit_04",
        name = "Amit Yadav",
        mobile = "+91 98456 78901",
        planName = "Student Plan",
        joinDate = DateUtils.getDateOffsetDays(-35),
        expiryDate = DateUtils.getDateOffsetDays(-5),
        totalFees = 998,
        paidAmount = 98,
        dueAmount = 900,
        status = MemberStatus.DUE,
        avatarInitial = "AY",
        daysOverdue = 5
      ),
      Member(
        id = "m_deepak_05",
        name = "Deepak Rao",
        mobile = "+91 98567 89012",
        planName = "General Plan",
        joinDate = DateUtils.getDateOffsetDays(-34),
        expiryDate = DateUtils.getDateOffsetDays(-4),
        totalFees = 1400,
        paidAmount = 600,
        dueAmount = 800,
        status = MemberStatus.DUE,
        avatarInitial = "DR",
        daysOverdue = 4
      ),
      Member(
        id = "m_sunil_06",
        name = "Sunil Gujjar",
        mobile = "+91 98678 90123",
        planName = "6 Month Plan",
        joinDate = DateUtils.getDateOffsetDays(-194),
        expiryDate = DateUtils.getDateOffsetDays(-14),
        totalFees = 3299,
        paidAmount = 1199,
        dueAmount = 2100,
        status = MemberStatus.DUE,
        avatarInitial = "SG",
        daysOverdue = 14
      ),

      // Expiring Soon Members (Within 7 days)
      Member(
        id = "m_rohit_07",
        name = "Rohit Sharma",
        mobile = "+91 98789 01234",
        planName = "General Plan",
        joinDate = DateUtils.getDateOffsetDays(-28),
        expiryDate = DateUtils.getDateOffsetDays(2),
        totalFees = 799,
        paidAmount = 799,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "RS"
      ),
      Member(
        id = "m_pooja_08",
        name = "Pooja Saini",
        mobile = "+91 98890 12345",
        planName = "Female Plan",
        joinDate = DateUtils.getDateOffsetDays(-27),
        expiryDate = DateUtils.getDateOffsetDays(3),
        totalFees = 699,
        paidAmount = 699,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "PS"
      ),
      Member(
        id = "m_priyanka_09",
        name = "Priyanka Sharma",
        mobile = "+91 98901 23456",
        planName = "Female Plan",
        joinDate = DateUtils.getDateOffsetDays(-29),
        expiryDate = DateUtils.getDateOffsetDays(1),
        totalFees = 699,
        paidAmount = 699,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "PS"
      ),
      Member(
        id = "m_neeraj_10",
        name = "Neeraj Chauhan",
        mobile = "+91 98012 34567",
        planName = "Student Plan",
        joinDate = DateUtils.getDateOffsetDays(-25),
        expiryDate = DateUtils.getDateOffsetDays(5),
        totalFees = 499,
        paidAmount = 499,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "NC"
      ),
      Member(
        id = "m_manish_11",
        name = "Manish Soni",
        mobile = "+91 98112 23344",
        planName = "General Plan",
        joinDate = DateUtils.getDateOffsetDays(-26),
        expiryDate = DateUtils.getDateOffsetDays(4),
        totalFees = 799,
        paidAmount = 799,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "MS"
      ),
      Member(
        id = "m_kavita_12",
        name = "Kavita Rao",
        mobile = "+91 98223 34455",
        planName = "Female Plan",
        joinDate = DateUtils.getDateOffsetDays(-24),
        expiryDate = DateUtils.getDateOffsetDays(6),
        totalFees = 699,
        paidAmount = 699,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "KR"
      ),
      Member(
        id = "m_suresh_13",
        name = "Suresh Yadav",
        mobile = "+91 98334 45566",
        planName = "General Plan",
        joinDate = DateUtils.getDateOffsetDays(-23),
        expiryDate = DateUtils.getDateOffsetDays(7),
        totalFees = 799,
        paidAmount = 799,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "SY"
      ),
      Member(
        id = "m_anita_14",
        name = "Anita Devi",
        mobile = "+91 98445 56677",
        planName = "Female Plan",
        joinDate = DateUtils.getDateOffsetDays(-30),
        expiryDate = DateUtils.getDateOffsetDays(0),
        totalFees = 699,
        paidAmount = 699,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "AD"
      )
    )

    // Additional due members to make exactly 24 due members and exactly ₹18,900 total dues:
    // Highlighted dues so far: 700 + 1500 + 1200 + 900 + 800 + 2100 = 7,200 (6 members)
    // Remaining due amount needed: 18,900 - 7,200 = 11,700 across 18 members = 18 * 650
    val additionalDueMembers = (1..18).map { i ->
      val dueAmt = 650
      Member(
        id = "m_due_extra_$i",
        name = "Member Due #$i",
        mobile = "+91 98555 ${10000 + i}",
        planName = "General Plan",
        joinDate = DateUtils.getDateOffsetDays(-40 - i),
        expiryDate = DateUtils.getDateOffsetDays(-2 - (i % 8)),
        totalFees = 1400,
        paidAmount = 750,
        dueAmount = dueAmt,
        status = MemberStatus.DUE,
        avatarInitial = "M$i",
        daysOverdue = 2 + (i % 8)
      )
    }

    // Additional expiring members to make exactly 12 expiring soon:
    // We already have 8 expiring members above. We need 4 more:
    val additionalExpiring = (1..4).map { i ->
      Member(
        id = "m_exp_extra_$i",
        name = "Expiring Member #$i",
        mobile = "+91 98666 ${20000 + i}",
        planName = "3 Month Power",
        joinDate = DateUtils.getDateOffsetDays(-85 + i),
        expiryDate = DateUtils.getDateOffsetDays(i + 2),
        totalFees = 1899,
        paidAmount = 1899,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "E$i"
      )
    }

    // Additional active non-expiring members:
    // Total needed active: 162.
    // We currently have: 8 (highlighted) + 4 (additionalExpiring) = 12 active members.
    // Need 162 - 12 = 150 active non-expiring members.
    val additionalActive = (1..150).map { i ->
      Member(
        id = "m_act_$i",
        name = "Active Member #$i",
        mobile = "+91 98777 ${30000 + i}",
        planName = when (i % 3) {
          0 -> "3 Month Power"
          1 -> "General Plan"
          else -> "1 Year Plan"
        },
        joinDate = DateUtils.getDateOffsetDays(-10 - (i % 50)),
        expiryDate = DateUtils.getDateOffsetDays(15 + (i % 120)),
        totalFees = 799,
        paidAmount = 799,
        dueAmount = 0,
        status = MemberStatus.ACTIVE,
        avatarInitial = "A$i"
      )
    }

    val allMembers = highlightedMembers + additionalDueMembers + additionalExpiring + additionalActive
    // Total members = 14 + 18 + 4 + 150 = 186 members!
    // Active members = 162 (12 expiring + 150 long-term = 162)
    // Due members = 24 (6 highlighted + 18 extra = 24)
    // Total due amount = 7,200 + 11,700 = ₹18,900!
    // Expiring soon count = 8 + 4 = 12!

    // Today's Payments:
    // Cash: ₹2,500
    // UPI: ₹5,450
    // Card: ₹500
    // Total: ₹8,450
    val todayPayments = listOf(
      PaymentRecord(
        id = "pay_t1",
        memberId = "m_rohit_07",
        memberName = "Rohit Sharma",
        amount = 799,
        paymentMethod = PaymentMethod.UPI,
        paymentDate = today,
        status = PaymentStatus.SUCCESS,
        invoiceNo = "INV-1001",
        notes = "Monthly renewal via GPay"
      ),
      PaymentRecord(
        id = "pay_t2",
        memberId = "m_vikas_02",
        memberName = "Vikas Yadav",
        amount = 1500,
        paymentMethod = PaymentMethod.CASH,
        paymentDate = today,
        status = PaymentStatus.SUCCESS,
        invoiceNo = "INV-1002",
        notes = "Fee part-payment reception"
      ),
      PaymentRecord(
        id = "pay_t3",
        memberId = "m_act_1",
        memberName = "Active Member #1",
        amount = 2651,
        paymentMethod = PaymentMethod.UPI,
        paymentDate = today,
        status = PaymentStatus.SUCCESS,
        invoiceNo = "INV-1003",
        notes = "Quarterly plan UPI"
      ),
      PaymentRecord(
        id = "pay_t4",
        memberId = "m_act_2",
        memberName = "Active Member #2",
        amount = 2000,
        paymentMethod = PaymentMethod.UPI,
        paymentDate = today,
        status = PaymentStatus.SUCCESS,
        invoiceNo = "INV-1004",
        notes = "Paytm QR scan"
      ),
      PaymentRecord(
        id = "pay_t5",
        memberId = "m_pooja_08",
        memberName = "Pooja Saini",
        amount = 1000,
        paymentMethod = PaymentMethod.CASH,
        paymentDate = today,
        status = PaymentStatus.SUCCESS,
        invoiceNo = "INV-1005",
        notes = "Reception cash counter"
      ),
      PaymentRecord(
        id = "pay_t6",
        memberId = "m_neeraj_10",
        memberName = "Neeraj Chauhan",
        amount = 500,
        paymentMethod = PaymentMethod.CARD,
        paymentDate = today,
        status = PaymentStatus.SUCCESS,
        invoiceNo = "INV-1006",
        notes = "POS Machine swiped"
      )
      // Total Cash: 1500 + 1000 = 2,500
      // Total UPI: 799 + 2651 + 2000 = 5,450
      // Total Card: 500
      // Total: 2,500 + 5,450 + 500 = ₹8,450!
    )

    // Yesterday's Payments totaling ₹7,540 (+12% vs yesterday!)
    val yesterdayPayments = listOf(
      PaymentRecord(
        id = "pay_y1",
        memberId = "m_act_10",
        memberName = "Yesterday Payer",
        amount = 7540,
        paymentMethod = PaymentMethod.UPI,
        paymentDate = yesterday,
        status = PaymentStatus.SUCCESS,
        invoiceNo = "INV-0998"
      )
    )

    val attendanceRecords = listOf(
      AttendanceRecord(
        id = "att_1",
        memberId = "m_rohit_07",
        memberName = "Rohit Sharma",
        planName = "General Plan",
        checkInTime = "06:30 AM",
        date = today
      ),
      AttendanceRecord(
        id = "att_2",
        memberId = "m_pooja_08",
        memberName = "Pooja Saini",
        planName = "Female Plan",
        checkInTime = "07:15 AM",
        date = today
      ),
      AttendanceRecord(
        id = "att_3",
        memberId = "m_neeraj_10",
        memberName = "Neeraj Chauhan",
        planName = "Student Plan",
        checkInTime = "07:45 AM",
        date = today
      )
    )

    return Triple(allMembers, todayPayments + yesterdayPayments, attendanceRecords)
  }
}
