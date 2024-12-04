package com.example.itelective3.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.itelective3.databinding.StudentAttendanceBinding
import com.example.itelective3.model.Checking

class AttendanceAdapter(private val studentList: MutableList<Checking>) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    class AttendanceViewHolder(private val binding: StudentAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Checking) {
            binding.studentName.text = student.studentName
            binding.checkBoxAttendance.isChecked = student.isChecked

            binding.checkBoxAttendance.setOnCheckedChangeListener { _, isChecked ->
                student.isChecked = isChecked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = StudentAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(studentList[position])
    }

    override fun getItemCount(): Int = studentList.size
}