package com.example.itelective3

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itelective3.adapter.StudentAdapter
import com.example.itelective3.databinding.ActivityClassDetailBinding
import com.example.itelective3.model.Student
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClassDetail : AppCompatActivity() {
    private lateinit var binding: ActivityClassDetailBinding
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var studentList: MutableList<Student>
    private val selectedStudents = mutableSetOf<Student>() // Track selected students
    private val database = FirebaseDatabase.getInstance().getReference("Students")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val classId = intent.getStringExtra("classId")
        val className = intent.getStringExtra("className") // Class name
        val subjectCode = intent.getStringExtra("subjectCode") // Subject code
        val schedule = intent.getStringExtra("schedule") // Schedule

        if (classId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Class ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.className.text = "$className"
        binding.subjectCode.text = "Subject Code: $subjectCode"
        binding.classSchedule.text = "Schedule: $schedule"

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.removeStudentsButton.setOnClickListener {
            removeSelectedStudents()
        }

        setupStudentsRecyclerView()
        loadStudents(classId)
    }
    private fun setupStudentsRecyclerView() {
        studentList = mutableListOf()
        studentAdapter = StudentAdapter(studentList, selectedStudents) { student ->
            Toast.makeText(this, "Clicked: ${student.studentName}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewStudents.apply {
            layoutManager = LinearLayoutManager(this@ClassDetail)
            adapter = studentAdapter
        }
    }
    private fun loadStudents(classId: String?) {
        database.orderByChild("classId").equalTo(classId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                studentList.clear()
                for (studentSnapshot in snapshot.children) {
                    studentSnapshot.getValue(Student::class.java)?.let { studentList.add(it) }
                }
                studentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClassDetail, "Failed to load students: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun removeSelectedStudents() {
        if (selectedStudents.isEmpty()) {
            Toast.makeText(this, "No students selected for removal.", Toast.LENGTH_SHORT).show()
        } else {
            // Show confirmation dialog
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete the selected students?")

        builder.setPositiveButton("Delete") { dialog, _ ->
            deleteSelectedStudents()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun deleteSelectedStudents() {
        for (student in selectedStudents) {
            val position = studentList.indexOf(student)
            if (position != -1) {
                studentList.removeAt(position)
                student.studentId?.let { id ->
                    database.child(id).removeValue()
                }
                studentAdapter.notifyItemRemoved(position)
            }
        }

        selectedStudents.clear()
        Toast.makeText(this, "Selected students removed successfully.", Toast.LENGTH_SHORT).show()
    }

}


