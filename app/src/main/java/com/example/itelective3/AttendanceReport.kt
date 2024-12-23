package com.example.itelective3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.itelective3.databinding.ActivityAttendanceReportBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.itelective3.model.AttendanceReport
import com.google.firebase.database.ValueEventListener

class AttendanceReport : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceReportBinding
    private lateinit var attendanceReportRef: DatabaseReference
    private lateinit var classList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classList = ArrayList()
        attendanceReportRef = FirebaseDatabase.getInstance().getReference("Classes")

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, TeacherMain::class.java)
            startActivity(intent)
            finish()
        }
        loadClassesIntoSpinner()

        binding.spinnerClassSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedClass = parent.getItemAtPosition(position).toString()
                    loadAttendanceDataForClass(selectedClass)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun loadClassesIntoSpinner() {
        attendanceReportRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                classList.clear()
                for (classSnapshot in snapshot.children) {
                    val className = classSnapshot.child("className").getValue(String::class.java)
                    if (className != null) {
                        classList.add(className)
                    }
                }

                val adapter = ArrayAdapter(
                    this@AttendanceReport,
                    android.R.layout.simple_spinner_item,
                    classList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerClassSelection.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadAttendanceDataForClass(selectedClass: String) {
        val attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance")
        attendanceRef.orderByChild("classId").equalTo(selectedClass)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.tableLayoutAttendanceReport.removeAllViews()

                    if (snapshot.exists()) {
                        for (attendanceSnapshot in snapshot.children) {
                            val report = attendanceSnapshot.getValue(AttendanceReport::class.java)
                            if (report != null) {
                                Log.d(
                                    "AttendanceReport",
                                    "Student: ${report.studentName}, Attendance: ${report.totalAttendance}"
                                )
                                addAttendanceRow(report)
                            }
                        }
                    } else {
                        Log.d("AttendanceReport", "No attendance data found for selected class.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AttendanceReport", "Error fetching data", error.toException())
                }
            })
    }


    private fun addAttendanceRow(report: AttendanceReport) {
        val row = TableRow(this)
        val studentName = TextView(this)
        val totalAttendance = TextView(this)
        val attendancePercentage = TextView(this)

        studentName.text = report.studentName
        totalAttendance.text = report.totalAttendance.toString()
        attendancePercentage.text = "${report.attendancePercentage}%"

        row.addView(studentName)
        row.addView(totalAttendance)
        row.addView(attendancePercentage)

        binding.tableLayoutAttendanceReport.addView(row)
    }
}
