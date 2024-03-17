package com.mrprogrammer.attendance

import android.app.ProgressDialog
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mrprogrammer.Utils.CommonFunctions.CommonFunctions
import com.mrprogrammer.Utils.CommonFunctions.LocalSharedPreferences
import com.mrprogrammer.Utils.Interface.CompleteHandler
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PostAttdance {

    companion object {
        fun post(context: Context, completeHandler: CompleteHandler){

            val result = isBefore9AMOrAfter9AM()


            if(!LocalFunctions.isConnected(context)) {
                completeHandler.onFailure("Please connect to internet ..")
            }

            if (!result) {
                completeHandler.onFailure("Session Closed....")
                return
            }
            val email = LocalSharedPreferences.getLocalSavedUser(context)?.get(1)
            val progressDialog: ProgressDialog = ProgressDialog(context)
            progressDialog.setMessage("Posting ...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            val db = FirebaseDatabase.getInstance().getReference("data").child(CommonFunctions.firebaseClearString(email).toString()).child(getKey())
            db.child("time").setValue(LocalFunctions.getTime())
            db.child("date").setValue(LocalFunctions.getDate())
            db.child("type").setValue("Normal")
            db.child("valid").setValue("true").addOnCompleteListener {
                progressDialog.dismiss()
                completeHandler.onSuccess("")
            }

        }

       private fun getKey(): String {
            val date = LocalDate.now()
            return "${date.dayOfMonth}${date.monthValue}${date.year}"
        }

        fun isBefore9AMOrAfter9AM(): Boolean {

            // Define a formatter for 12-hour format
            val formatter = DateTimeFormatter.ofPattern("hh:mm a")

            // Format the current time using the formatter
            val formattedTimeStr = LocalTime.now().format(formatter)

            // Parse the formatted time string back into a LocalTime object
            val currentTime = LocalTime.parse(formattedTimeStr, formatter)


            val model = ObjectHolder.getInstance()?.getAttdanceModel()?.value

            val timeList = model?.timeStart?.split(" ")
            val start = timeList?.get(0)?.split(":")?.plus(timeList[1])

            val timeList1 = model?.timeEnd?.split(" ")
            val end = timeList1?.get(0)?.split(":")?.plus(timeList1[1])

            val s1 = LocalTime.of(start?.get(0)?.toInt()?: 9, start?.get(1)?.toInt()?: 0)
            val e1 = LocalTime.of(end?.get(0)?.toInt()?: 9, end?.get(1)?.toInt()?: 0)

            return currentTime.isAfter(s1) && currentTime.isBefore(e1)
        }
    }

}