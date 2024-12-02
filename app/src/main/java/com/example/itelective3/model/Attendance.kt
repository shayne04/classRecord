package com.example.itelective3.model

data class Attendance(
    val uid: String? = null,
    val attendanceId: String? = null,
    val classId: String? = null,
    val studentId: String? = null,
    val date: String? = null,
    var status: String? = null
)
