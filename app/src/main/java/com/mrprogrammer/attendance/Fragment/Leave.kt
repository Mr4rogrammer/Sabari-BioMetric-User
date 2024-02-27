package com.mrprogrammer.attendance.Fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mrprogrammer.Utils.CommonFunctions.LocalSharedPreferences
import com.mrprogrammer.Utils.Widgets.ProgressButton
import com.mrprogrammer.attendance.Adapter.LeaveListAdapter
import com.mrprogrammer.attendance.R
import com.mrprogrammer.attendance.Utils
import com.mrprogrammer.attendance.Utils.Companion.firebaseClearString
import com.mrprogrammer.attendance.Utils.Companion.getCurrentDate
import com.mrprogrammer.attendance.Utils.Companion.getCurrentTime
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder
import info.mrprogrammer.admin_bio.Model.LeaveDataMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class Leave : Fragment() {
    lateinit var adapter: LeaveListAdapter
    private var listOfData = mutableListOf<LeaveDataMode>()
    var items = arrayOf("Leave", "On Duty","Permission")
    lateinit var root:View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root =  inflater.inflate(R.layout.leave_fragment, container, false)
        initSpinner()
        initData()
        initRe()
        return root
    }

    private fun initRe() {
        val recyclerView: RecyclerView =root.findViewById(R.id.leave)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        adapter = LeaveListAdapter(requireContext(), listOfData)
        recyclerView.adapter = adapter
        getListOfUser()
    }

    private fun getListOfUser() {
        val db = FirebaseDatabase.getInstance().getReference("leave").child("krish@gmaicom")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val email = LocalSharedPreferences.getLocalSavedUser(requireActivity())?.get(1)
                listOfData.clear()
                snapshot.children.forEach {
                    val data = it.getValue(LeaveDataMode::class.java)
                    data?.key = it.key.toString()
                    if (data != null && data.email == email) {
                        listOfData.add(data)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun initData() {
        val start = root.findViewById<TextView>(R.id.start_date)
        val end = root.findViewById<TextView>(R.id.end_date)
        val button = root.findViewById<ProgressButton>(R.id.button)

        val timeStart = root.findViewById<TextView>(R.id.start_time)
        val timeEnd = root.findViewById<TextView>(R.id.end_time)

        start.text = getCurrentDate()
        end.text = getCurrentDate()

        timeStart.text = getCurrentTime()
        timeEnd.text = "${getCurrentTime()}"


        start.setOnClickListener {
            dateCicker(start)
        }

        end.setOnClickListener {
            dateCicker(end)
        }

        timeEnd.setOnClickListener {
            showTimePickerDialog(timeEnd)
        }

        timeStart.setOnClickListener {
            showTimePickerDialog(timeStart)
        }

        button.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Enter Reason Text")

            val input = EditText(context)
            builder.setView(input)
            builder.setPositiveButton("OK") { _, _ ->
                val enteredText = input.text.toString()
                val roll =  Utils.retrieveData(requireContext(), "roll")
                val email = LocalSharedPreferences.getLocalSavedUser(requireContext())?.get(1)
                val db = FirebaseDatabase.getInstance().getReference("leave").child("krish@gmaicom")
                val clearMail = email?.firebaseClearString().toString()
                db.child(clearMail).child("adminremark").setValue("")
                db.child(clearMail).child("email").setValue(email)
                db.child(clearMail).child("enddate").setValue(end.text.toString())
                db.child(clearMail).child("endtime").setValue(timeEnd.text.toString())
                db.child(clearMail).child("fromdate").setValue(start.text.toString())
                db.child(clearMail).child("fromtime").setValue(timeStart.text.toString())
                db.child(clearMail).child("reason").setValue(enteredText)
                db.child(clearMail).child("roll").setValue(roll)
                db.child(clearMail).child("status").setValue(false).addOnCompleteListener {
                    ObjectHolder.getInstance()?.MrToast()?.success(requireActivity(),"Applied")
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()

        }
    }


    private fun dateCicker(textView: TextView) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a Date")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it
            val selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(calendar.time)
            textView.text = selectedDate
        }
        datePicker.show(requireActivity().supportFragmentManager,"j")
    }



    private fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    fun showTimePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val timeText = formatTime(selectedHour, selectedMinute)

                textView.text = timeText
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }


    private fun initSpinner() {
        val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner: Spinner = root.findViewById(R.id.spinner)
        spinner.adapter = adapter
    }
}
