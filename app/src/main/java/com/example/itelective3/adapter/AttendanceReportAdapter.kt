package com.example.itelective3.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.itelective3.databinding.ItemAttendanceReportBinding
import com.example.itelective3.model.AttendanceReport

class AttendanceReportAdapter (private val reports: ArrayList<AttendanceReport>) :
    RecyclerView.Adapter<AttendanceReportAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAttendanceReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(report: AttendanceReport) {
            binding.tvStudentName.text = report.studentName
            binding.tvTotalAttendance.text = "${report.totalAttendance}/100"
            binding.tvAttendancePercentage.text = "${report.attendancePercentage}%"
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount(): Int = reports.size
}
