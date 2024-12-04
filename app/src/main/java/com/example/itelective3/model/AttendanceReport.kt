package com.example.itelective3.model

data class AttendanceReport(
    val uid: String? = null,
    val studentId: String? = null,
    val studentName: String? = null,
    val totalAttendance: Int? = null,
    val attendancePercentage: Int? = null,
    val classId: String? = null
)
