package com.example.itelective3


import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itelective3.adapter.AttendanceAdapter
import com.example.itelective3.databinding.FragmentAttendanceBinding
import com.example.itelective3.model.Checking
import com.example.itelective3.model.Attendance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.UUID

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var attendanceAdapter: AttendanceAdapter
    private lateinit var studentList: MutableList<Checking>
    private var selectedClassId: String? = null
    private var selectedDate: String? = null
    private lateinit var teacherUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teacherUid = arguments?.getString("TEACHER_UID") ?: ""

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        studentList = mutableListOf()
        attendanceAdapter = AttendanceAdapter(studentList)
        binding.recyclerAttendanceStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAttendanceStudents.adapter = attendanceAdapter

        loadClasses()

        binding.spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedClassId = parent?.getItemAtPosition(position).toString()
                selectedClassId?.let { loadStudentsForClass(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.attendanceDate.setOnClickListener { showDatePicker() }
        binding.saveAttendanceButton.setOnClickListener { saveAttendance() }
    }
    private fun loadClasses() {
        database.reference.child("Classes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val classNames = mutableListOf<String>()
                val classIdMap = mutableMapOf<String, String>()

                for (classSnapshot in snapshot.children) {
                    val className = classSnapshot.child("className").getValue(String::class.java) ?: ""
                    val classId = classSnapshot.key ?: ""
                    if (className.isNotEmpty() && classId.isNotEmpty()) {
                        classNames.add(className)
                        classIdMap[className] = classId
                    }
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, classNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerClass.adapter = adapter

                binding.spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedClassName = parent?.getItemAtPosition(position).toString()
                        selectedClassId = classIdMap[selectedClassName]
                        selectedClassId?.let { loadStudentsForClass(it) }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load classes.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun loadStudentsForClass(classId: String) {
        database.reference.child("Students").orderByChild("classId").equalTo(classId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    studentList.clear()
                    for (studentSnapshot in snapshot.children) {
                        val student = studentSnapshot.getValue(Checking::class.java)
                        student?.let { studentList.add(it) }
                    }
                    attendanceAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load students.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            if (selectedYear >= 2024) {
                selectedDate = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                binding.attendanceDate.text = selectedDate
            } else {
                Toast.makeText(requireContext(), "Please select a year starting from 2024", Toast.LENGTH_SHORT).show()
            }
        }, year, month, day)

        val startDate = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1)
        }

        datePickerDialog.datePicker.minDate = startDate.timeInMillis

        datePickerDialog.show()
    }

    private fun saveAttendance() {
        if (selectedClassId.isNullOrEmpty() || selectedDate.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select class and date.", Toast.LENGTH_SHORT).show()
            return
        }

        val attendanceRecords = mutableListOf<Attendance>()
        for (student in studentList) {
            if (student.isChecked) {
                val attendance = Attendance(
                    uid = teacherUid,
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
            Toast.makeText(requireContext(), "Attendance saved successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No students marked present.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
