package com.example.itelective3.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.itelective3.databinding.ItemClassCardBinding
import com.example.itelective3.model.Class

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
            // Update button listener (add this part for updating)
            binding.updateClassButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val classObj = classList[position]
                    onUpdateClass(classObj) // Trigger update callback
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

        holder.itemView.setOnClickListener { onClassClick(classObj) }
    }

    override fun getItemCount() = classList.size


}