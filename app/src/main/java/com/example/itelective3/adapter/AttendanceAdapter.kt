package com.example.itelective3.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.itelective3.databinding.StudentAttendanceBinding
import com.example.itelective3.model.Checking

class AttendanceAdapter(private val studentList: MutableList<Checking>) : RecyclerView.Adapter<AttendanceAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = StudentAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentList[position]
        holder.binding.studentName.text = student.fullName // Assuming there is a TextView for the student's name
        holder.binding.checkBoxAttendance.isChecked = student.isChecked

        holder.binding.checkBoxAttendance.setOnCheckedChangeListener { _, isChecked ->
            student.isChecked = isChecked
        }
    }

    override fun getItemCount(): Int = studentList.size

    inner class StudentViewHolder(val binding: StudentAttendanceBinding) : RecyclerView.ViewHolder(binding.root)
}