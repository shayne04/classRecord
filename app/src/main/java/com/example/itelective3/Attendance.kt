package com.example.itelective3


import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itelective3.adapter.AttendanceAdapter
import com.example.itelective3.databinding.ActivityAttendanceBinding
import com.example.itelective3.model.Checking
import com.example.itelective3.model.Attendance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.UUID


class Attendance : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var attendanceAdapter: AttendanceAdapter
    private lateinit var studentList: MutableList<Checking>
    private var selectedClassId: String? = null
    private var selectedDate: String? = null
    private lateinit var teacherUid: String // Teacher's UID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        teacherUid = intent.getStringExtra("TEACHER_UID") ?: ""

        if (teacherUid.isEmpty()) {
            Toast.makeText(this, "Teacher UID not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize RecyclerView
        studentList = mutableListOf()
        attendanceAdapter = AttendanceAdapter(studentList)
        binding.recyclerViewStudents.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewStudents.adapter = attendanceAdapter

        // Load Classes in Spinner
        loadClasses()

        // Spinner class selection
        binding.spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedClassId = parent?.getItemAtPosition(position).toString()
                loadStudentsForClass(selectedClassId!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where no class is selected, if necessary
            }
        }
        // Date Picker
        binding.attendanceDate.setOnClickListener { showDatePicker() }

        // Save Attendance
        binding.btnSaveAttendance.setOnClickListener { saveAttendance() }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, TeacherMain::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadClasses() {
        database.reference.child("Classes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val classNames = mutableListOf<String>()
                val classIdMap = mutableMapOf<String, String>() // To store class name and class ID

                for (classSnapshot in snapshot.children) {
                    val className = classSnapshot.child("className").getValue(String::class.java) ?: ""
                    val classId = classSnapshot.key ?: ""
                    if (className.isNotEmpty() && classId.isNotEmpty()) {
                        classNames.add(className)
                        classIdMap[className] = classId // Store class name and corresponding class ID
                    }
                }

                val adapter = ArrayAdapter(this@Attendance, android.R.layout.simple_spinner_item, classNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerClass.adapter = adapter

                // When a class is selected, set the selected class ID
                binding.spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedClassName = parent?.getItemAtPosition(position).toString()
                        selectedClassId = classIdMap[selectedClassName] // Retrieve the class ID
                        loadStudentsForClass(selectedClassId!!)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Handle the case where no class is selected, if necessary
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Attendance, "Failed to load classes.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadStudentsForClass(classId: String) {
        database.reference.child("Students").child(classId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                studentList.clear()
                for (studentSnapshot in snapshot.children) {
                    val student = studentSnapshot.getValue(Checking::class.java)
                    if (student != null) {
                        studentList.add(student)
                        // Log student details
                        println("Student Loaded: ${student.fullName}")
                    } else {
                        println("Error: Student is null")
                    }
                }
                attendanceAdapter.notifyDataSetChanged() // Update the adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Attendance, "Failed to load students.", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedMonth/${selectedDay + 1}/$selectedYear"
            binding.attendanceDate.text = selectedDate
        }, year, month, day).show()
    }

    private fun saveAttendance() {
        if (selectedClassId.isNullOrEmpty() || selectedDate.isNullOrEmpty()) {
            Toast.makeText(this, "Please select class and date.", Toast.LENGTH_SHORT).show()
            return
        }

        val attendanceRecords = mutableListOf<Attendance>()
        for (student in studentList) {
            if (student.isChecked) {
                val attendance = Attendance(
                    uid = teacherUid, // Use teacher's UID
                    attendanceId = UUID.randomUUID().toString(),
                    classId = selectedClassId,
                    studentId = student.uid,
                    date = selectedDate,
                    status = "Present"
                )
                attendanceRecords.add(attendance)
            }
        }

        if (attendanceRecords.isNotEmpty()) {
            for (attendance in attendanceRecords) {
                database.reference.child("Attendance")
                    .child(attendance.classId!!)
                    .child(attendance.attendanceId!!)
                    .setValue(attendance)
            }
            Toast.makeText(this, "Attendance saved successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No students marked present.", Toast.LENGTH_SHORT).show()
        }
    }
}