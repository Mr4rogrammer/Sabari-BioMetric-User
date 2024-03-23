package com.mrprogrammer.attendance

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mrprogrammer.Utils.CommonFunctions.LocalSharedPreferences
import com.mrprogrammer.attendance.Respositoy.CommonRepo
import com.mrprogrammer.attendance.Utils.Companion.firebaseClearString
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder
import info.mrprogrammer.admin_bio.Model.AttdanceModel
import info.mrprogrammer.admin_bio.Model.LeaveDataMode

class DataUtils {
    companion object {

        fun updateDataToUi(context: Context) {
            updateLeave(context)
            getDataAndStoreLocally()
        }
        private fun getDataAndStoreLocally() {
            val db = FirebaseDatabase.getInstance().getReference("attdances").child("data")
            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(AttdanceModel::class.java)?.let { ObjectHolder.getInstance()?.setAttdanceModel(it) }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }


        fun getDataAndSpAttdanceModel(function:(AttdanceModel?) -> Unit) {
            val db = FirebaseDatabase.getInstance().getReference("attdances").child("spdata")
            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                   function.invoke(snapshot.getValue(AttdanceModel::class.java))
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        private fun updateLeave(context: Context) {
            val listOfData = mutableListOf<LeaveDataMode>()
            val email = LocalSharedPreferences.getLocalSavedUser(context)?.get(1)
            val db = FirebaseDatabase.getInstance().getReference("leave").child(email?.firebaseClearString().toString())
            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var leaveCount = 0
                    var odCount = 0
                    var permissionCount = 0

                    val email = LocalSharedPreferences.getLocalSavedUser(context)?.get(1)
                    listOfData.clear()
                    snapshot.children.forEach {
                        val data = it.getValue(LeaveDataMode::class.java)
                        data?.key = it.key.toString()
                        if (data != null && data.email == email)    {
                            listOfData.add(data)
                        }
                        if(data?.status == true) {
                            when(data.type) {
                                "LEAVE" -> leaveCount +=1
                                "ON DUTY" -> odCount +=1
                                "PERMISSION" -> permissionCount +=1
                            }
                        }
                    }

                    val data: HashMap<String,String> = hashMapOf()
                    data["LEAVE"] = leaveCount.toString()
                    data["ON DUTY"] = odCount.toString()
                    data["PERMISSION"] = permissionCount.toString()

                    CommonRepo.updateLeave(data)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}