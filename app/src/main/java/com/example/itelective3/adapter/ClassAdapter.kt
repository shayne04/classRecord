package com.example.itelective3.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.itelective3.databinding.DialogAddStudentBinding
import com.example.itelective3.databinding.ItemClassCardBinding
import com.example.itelective3.model.Class
import com.example.itelective3.model.Student
import com.google.firebase.database.FirebaseDatabase

class ClassAdapter(
    private val classList: List<Class>,
    private val onClassClick: (Class) -> Unit,
    private val onDeleteClick: (Class) -> Unit,
    private val onUpdateClass: (Class) -> Unit
) : RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {

    inner class ClassViewHolder(val binding: ItemClassCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.deleteClass.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val classObj = classList[position]
                    onDeleteClick(classObj)
                }
            }

            binding.updateClassButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val classObj = classList[position]
                    onUpdateClass(classObj)
                }
            }
            binding.addStudentsButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val classObj = classList[position]
                    showAddStudentDialog(binding.root.context, classObj)
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val binding = ItemClassCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClassViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val classObj = classList[position]

        holder.binding.className.text = classObj.className
        holder.binding.subjectCode.text = "Subject Code: ${classObj.subjectCode}"
        holder.binding.classSchedule.text = "Schedule: ${classObj.day}, ${classObj.time}"

        holder.itemView.setOnClickListener {
            onClassClick(classObj)
        }
    }

    override fun getItemCount() = classList.size

    private fun showAddStudentDialog(context: Context, classObj: Class) {
        val dialogBinding = DialogAddStudentBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setTitle("Add Student to ${classObj.className}")
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialogBinding.addStudentSaveButton.setOnClickListener {
            val studentId = dialogBinding.studentId.text.toString().trim()
            val studentName = dialogBinding.studentName.text.toString().trim()

            if (studentId.isEmpty() || studentName.isEmpty()) {
                Toast.makeText(context, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            } else {
                val student = Student(
                    uid = classObj.uid,
                    classId = classObj.classID,
                    studentId = studentId,
                    studentName = studentName
                )
                saveStudentToDatabase(context, student, dialog)
            }
        }

        dialog.show()
    }

    private fun saveStudentToDatabase(context: Context, student: Student, dialog: AlertDialog) {
        val database = FirebaseDatabase.getInstance().getReference("Students")
        database.child(student.studentId!!).setValue(student).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Student added successfully!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Failed to add student.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}