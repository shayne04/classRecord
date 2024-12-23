package com.example.itelective3


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itelective3.adapter.AttendanceReportAdapter
import com.example.itelective3.databinding.FragmentAttendanceReportBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.example.itelective3.model.AttendanceReport
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AttendanceReportFragment : Fragment() {

    private var _binding: FragmentAttendanceReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private lateinit var attendanceReportRef: DatabaseReference
    private lateinit var classList: ArrayList<String>
    private lateinit var attendanceReports: ArrayList<AttendanceReport>
    private lateinit var adapter: AttendanceReportAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceReportBinding.inflate(inflater, container, false)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        attendanceReportRef = database.getReference("AttendanceReports")
        classList = arrayListOf()
        attendanceReports = arrayListOf()

        // Setup RecyclerView
        binding.recyclerViewAttendanceReport.layoutManager = LinearLayoutManager(requireContext())
        adapter = AttendanceReportAdapter(attendanceReports)
        binding.recyclerViewAttendanceReport.adapter = adapter

        // Load Classes
        loadClasses()

        // Set Spinner Listener
        binding.spinnerClassSelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedClassId = classList[position]
                    loadAttendanceReports(selectedClassId)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        return binding.root
    }

    private fun loadClasses() {
        // Fetch classes for the spinner
        database.getReference("Classes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                classList.clear()
                val classNames = arrayListOf<String>()

                for (classSnapshot in snapshot.children) {
                    val classId = classSnapshot.key
                    val className = classSnapshot.child("className").getValue(String::class.java)

                    if (classId != null && className != null) {
                        classList.add(classId)
                        classNames.add(className)
                    }
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    classNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerClassSelector.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadAttendanceReports(classId: String) {
        attendanceReportRef.orderByChild("classId").equalTo(classId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    attendanceReports.clear()
                    var totalAttendance = 0
                    var totalStudents = 0

                    for (reportSnapshot in snapshot.children) {
                        val report = reportSnapshot.getValue(AttendanceReport::class.java)
                        report?.let {
                            attendanceReports.add(it)
                            totalAttendance += it.totalAttendance ?: 0
                            totalStudents++
                        }
                    }

                    val averageAttendance =
                        if (totalStudents > 0) (totalAttendance * 100) / (totalStudents * totalAttendance) else 0
                    binding.tvTotalStudents.text = "Total Students: $totalStudents"
                    binding.tvAverageAttendance.text = "Average Attendance: $averageAttendance%"

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
 override fun onDestroyView() {
     super.onDestroyView()
     _binding = null
 }
}


