package com.example.itelective3.model

data class AttendanceReport(
    val uid: String? = null,
    val attendanceId: String? = null,
    val studentId: String? = null,
    var studentName: String? = null,
    val totalAttendance: Int? = null,
    val attendancePercentage: Int? = null,
    val classId: String? = null
)
