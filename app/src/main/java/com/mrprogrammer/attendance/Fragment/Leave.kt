package com.mrprogrammer.attendance.Fragment

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mrprogrammer.Utils.CommonFunctions.CommonFunctions
import com.mrprogrammer.Utils.CommonFunctions.LocalSharedPreferences
import com.mrprogrammer.Utils.Widgets.ProgressButton
import com.mrprogrammer.attendance.Adapter.LeaveListAdapter
import com.mrprogrammer.attendance.R
import com.mrprogrammer.attendance.Respositoy.CommonRepo
import com.mrprogrammer.attendance.SentNotification
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
    var spinner: Spinner? = null
    var selectedType:String = "Leave".toUpperCase()

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
        val email = LocalSharedPreferences.getLocalSavedUser(requireContext())?.get(1)
        val db = FirebaseDatabase.getInstance().getReference("leave").child(email?.firebaseClearString().toString())
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var leaveCount = 0
                var odCount = 0
                var permissionCount = 0

                val email = LocalSharedPreferences.getLocalSavedUser(requireActivity())?.get(1)
                listOfData.clear()
                snapshot.children.forEach {
                    val data = it.getValue(LeaveDataMode::class.java)
                    data?.key = it.key.toString()
                    if (data != null && data.email == email)    {
                        listOfData.add(data)
                    }
                    when(data?.type) {
                        "LEAVE" -> leaveCount +=1
                        "ON DUTY" -> odCount +=1
                        "PERMISSION" -> permissionCount +=1
                    }
                }

                val data: HashMap<String,String> = hashMapOf()
                data["LEAVE"] = leaveCount.toString()
                data["ON DUTY"] = odCount.toString()
                data["PERMISSION"] = permissionCount.toString()

                CommonRepo.updateLeave(data)
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
                val key = CommonFunctions.createUniqueKey().toString()
                val enteredText = input.text.toString()
                val roll =  Utils.retrieveData(requireContext(), "roll")
                val email = LocalSharedPreferences.getLocalSavedUser(requireContext())?.get(1)
                var db = FirebaseDatabase.getInstance().getReference("leave")
                val clearMail = email?.firebaseClearString().toString()
                db.child(clearMail).child(key).child("adminremark").setValue("")
                db.child(clearMail).child(key).child("email").setValue(email)
                db.child(clearMail).child(key).child("enddate").setValue(end.text.toString())
                db.child(clearMail).child(key).child("endtime").setValue(timeEnd.text.toString())
                db.child(clearMail).child(key).child("fromdate").setValue(start.text.toString())
                db.child(clearMail).child(key).child("fromtime").setValue(timeStart.text.toString())
                db.child(clearMail).child(key).child("reason").setValue(enteredText)
                db.child(clearMail).child(key).child("roll").setValue(roll)
                db.child(clearMail).child(key).child("type").setValue(selectedType)
                db.child(clearMail).child(key).child("status").setValue(false).addOnCompleteListener {
                    ObjectHolder.getInstance()?.MrToast()?.success(requireActivity(),"Applied")
                }

                sentNotfication(email)
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()

        }
    }


    private fun sentNotfication(email: String?) {
        SentNotification("token", getString(R.string.app_name), "$email applied Leave").execute()
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
            { _, selectedHour, selectedMinute ->
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
        spinner = root.findViewById(R.id.spinner)
        spinner?.adapter = adapter

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                selectedType = selectedItem.toUpperCase()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }
}
