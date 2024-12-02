package com.example.itelective3.model

data class Checking(
    val uid: String? = null,
    val fullName: String? = null, // Example property for student name
    var isChecked: Boolean = false // Tracks attendance
)
