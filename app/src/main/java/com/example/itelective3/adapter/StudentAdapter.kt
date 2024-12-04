package com.example.itelective3.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.itelective3.databinding.ItemStudentBinding
import com.example.itelective3.model.Student

class StudentAdapter(
    private val studentList: List<Student>,
    private val selectedStudents: MutableSet<Student>,
    private val onStudentClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(private val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {
            binding.studentId.text = student.studentId
            binding.studentName.text = student.studentName

            binding.checkBoxSelectStudent.isChecked = selectedStudents.contains(student)

            binding.checkBoxSelectStudent.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedStudents.add(student)
                } else {
                    selectedStudents.remove(student)
                }
            }

            binding.root.setOnClickListener {
                onStudentClick(student)

            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentList[position]
        holder.bind(student)

    }


    override fun getItemCount() = studentList.size

}