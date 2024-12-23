package com.example.itelective3


import android.R
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itelective3.adapter.ClassAdapter
import com.example.itelective3.databinding.DialogAddClassBinding
import com.example.itelective3.databinding.DialogUpdateClassBinding
import com.example.itelective3.databinding.FragmentTeacherHomeBinding
import com.example.itelective3.model.Class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class TeacherHomeFragment : Fragment() {
    private var _binding: FragmentTeacherHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var classAdapter: ClassAdapter
    private val classList: MutableList<Class> = mutableListOf()

    private val teacherUid: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        classAdapter = ClassAdapter(classList, { selectedClass ->
            navigateToClassDetail(selectedClass)
        }, { classToDelete ->
            deleteClass(classToDelete)
        }, { classToUpdate ->
            showUpdateClassDialog(classToUpdate)
        })

        binding.recyclerViewClasses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = classAdapter
        }

        binding.cardAddClass.setOnClickListener {
            showAddClassDialog()
        }

        loadClasses()
        loadTotalClassesCount()
        loadTotalStudentsCount()
    }

    private fun loadTotalClassesCount() {
        val database = FirebaseDatabase.getInstance().getReference("Classes")
        database.orderByChild("uid").equalTo(teacherUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totalClasses = snapshot.childrenCount
                    binding.totalClasses.text = "Classes: $totalClasses"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load class count.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadTotalStudentsCount() {
        val database = FirebaseDatabase.getInstance().getReference("Students")
        database.orderByChild("uid").equalTo(teacherUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totalStudents = snapshot.childrenCount
                    binding.totalStudents.text = "Students: $totalStudents"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load student count.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun navigateToClassDetail(selectedClass: Class) {
        val teacherUid = FirebaseAuth.getInstance().currentUser?.uid
        if (teacherUid != null) {
            val intent = Intent(requireContext(), ClassDetail::class.java).apply {
                putExtra("classId", selectedClass.classID)
                putExtra("className", selectedClass.className)
                putExtra("subjectCode", selectedClass.subjectCode)
                putExtra("schedule", "${selectedClass.day}, ${selectedClass.time}")
                putExtra("teacherUid", teacherUid)
            }
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "No teacher logged in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadClasses() {
        val database = FirebaseDatabase.getInstance().getReference("Classes")
        database.orderByChild("uid").equalTo(teacherUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    classList.clear()
                    for (child in snapshot.children) {
                        val classObj = child.getValue(Class::class.java)
                        classObj?.let { classList.add(it) }
                    }
                    classAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load classes.", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    private fun deleteClass(classToDelete: Class) {
        val classID = classToDelete.classID
        if (classID.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid Class ID.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Class")
            .setMessage("Are you sure you want to delete this class?")
            .setCancelable(true)
            .setPositiveButton("Yes") { _, _ ->
                val database = FirebaseDatabase.getInstance().getReference("Classes")
                database.child(classToDelete.classID).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Class deleted successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadClasses()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to delete class.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }
    private fun showAddClassDialog() {
        val dialogBinding = DialogAddClassBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("ADD CLASS")
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

    val daysOfWeek = arrayOf("MT", "WTh", "Friday", "Saturday")
    val dayAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, daysOfWeek)
    dayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
    dialogBinding.classDaySpinner.adapter = dayAdapter

    val classTimes = arrayOf(
        "8:00 AM - 10:00 AM",
        "10:00 AM - 12:00 PM",
        "1:00 PM - 3:00 PM",
        "3:00 PM - 5:00 PM"
    )
    val timeAdapter =
        ArrayAdapter(requireContext(), R.layout.simple_spinner_item, classTimes)
    timeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
    dialogBinding.classTimeSpinner.adapter = timeAdapter

    dialogBinding.addClassSaveButton.setOnClickListener {
        val classID = dialogBinding.classId.text.toString().trim()
        val className = dialogBinding.className.text.toString().trim()
        val subjectCode = dialogBinding.subjectCode.text.toString().trim()
        val day = dialogBinding.classDaySpinner.selectedItem.toString().trim()
        val time =
            dialogBinding.classTimeSpinner.selectedItem.toString().trim()

        if (classID.isEmpty() || className.isEmpty() || subjectCode.isEmpty() || day.isEmpty() || time.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT)
                .show()
        } else {
            checkClassIdUniquenessAndSave(classID, className, subjectCode, day, time, dialog)
        }
    }
    dialog.show()
}
    private fun checkClassIdUniquenessAndSave(
        classID: String,
        className: String,
        subjectCode: String,
        day: String,
        time: String,
        dialog: AlertDialog
    ) {
        val database = FirebaseDatabase.getInstance().getReference("Classes")
        database.child(classID).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result.exists()) {
                    Toast.makeText(requireContext(), "Class ID already exists. Please use a different ID.", Toast.LENGTH_SHORT).show()
                } else {
                    saveClassToDatabase(classID, className, subjectCode, day, time)
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "Error checking Class ID. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveClassToDatabase(
        classID: String,
        className: String,
        subjectCode: String,
        day: String,
        time: String
    ) {

        val classObj = Class(
            uid = teacherUid,
            classID = classID,
            className = className,
            subjectCode = subjectCode,
            day = day,
            time = time
        )

        val database = FirebaseDatabase.getInstance().getReference("Classes")
        database.child(classID).setValue(classObj).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Class added successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to add class. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUpdateClassDialog(classToUpdate: Class) {
        val dialogBinding = DialogUpdateClassBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("UPDATE CLASS")
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialogBinding.classId.setText(classToUpdate.classID)
        dialogBinding.className.setText(classToUpdate.className)
        dialogBinding.subjectCode.setText(classToUpdate.subjectCode)

        val daysOfWeek = arrayOf("MT", "WTh", "Friday", "Saturday")
        val dayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysOfWeek)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.classDaySpinner.adapter = dayAdapter
        val timeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayOf(
            "8:00 AM - 10:00 AM",
            "10:00 AM - 12:00 PM",
            "1:00 PM - 3:00 PM",
            "3:00 PM - 5:00 PM"
        ))
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.classTimeSpinner.adapter = timeAdapter

        // Set the current day and time from the class to be updated
        val dayPosition = daysOfWeek.indexOf(classToUpdate.day)
        dialogBinding.classDaySpinner.setSelection(if (dayPosition >= 0) dayPosition else 0)

        val timePosition = arrayOf(
            "8:00 AM - 10:00 AM",
            "10:00 AM - 12:00 PM",
            "1:00 PM - 3:00 PM",
            "3:00 PM - 5:00 PM"
        ).indexOf(classToUpdate.time)
        dialogBinding.classTimeSpinner.setSelection(if (timePosition >= 0) timePosition else 0)

        dialogBinding.updateClassSave.setOnClickListener {
            val classID = dialogBinding.classId.text.toString().trim()
            val className = dialogBinding.className.text.toString().trim()
            val subjectCode = dialogBinding.subjectCode.text.toString().trim()
            val day = dialogBinding.classDaySpinner.selectedItem.toString().trim()
            val time = dialogBinding.classTimeSpinner.selectedItem.toString().trim()

            if (classID.isEmpty() || className.isEmpty() || subjectCode.isEmpty() || day.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            } else {
                updateClassInDatabase(classToUpdate.classID, classID, className, subjectCode, day, time)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
    private fun updateClassInDatabase(
        originalClassID: String?,
        classID: String,
        className: String,
        subjectCode: String,
        day: String,
        time: String
    ) {

        val validClassID = originalClassID ?: classID

        val classObj = Class(
            uid = teacherUid,
            classID = classID,
            className = className,
            subjectCode = subjectCode,
            day = day,
            time = time
        )

        val database = FirebaseDatabase.getInstance().getReference("Classes")
        database.child(validClassID).setValue(classObj).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Class updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to update class. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


