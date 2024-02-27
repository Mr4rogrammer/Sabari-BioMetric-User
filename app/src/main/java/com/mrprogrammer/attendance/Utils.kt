package com.mrprogrammer.attendance

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import info.mrprogrammer.admin_bio.Model.AttdanceModel
import java.text.SimpleDateFormat
import java.util.*


class Utils {
    companion object {

        fun String.firebaseClearString(): String? {
            var aString = this
            aString = aString.replace("@", "").toString()
            aString = aString.replace(".", "").toString()
            aString = aString.replace("#", "").toString()
            aString = aString.replace("$", "").toString()
            aString = aString.replace("[", "").toString()
            aString = aString.replace("]", "").toString()
            return aString
        }
        fun showDialog(context: Context,message:String) {
            MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        fun getCurrentDate(): String {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            return dateFormat.format(calendar.time)
        }

         fun getCurrentTime(): String {
            val calendar = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
            return timeFormat.format(calendar.time)
        }


        fun getDataAndStoreLocally(function: (data:AttdanceModel) -> Unit) {
            val db = FirebaseDatabase.getInstance().getReference("attdances").child("data")
            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(AttdanceModel::class.java)?.let { function.invoke(it) }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }


         fun saveData(context: Context,key:String,value:String) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(key, value)
            editor.apply()
        }

         fun retrieveData(context: Context, key: String): String? {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
            val username = sharedPreferences.getString(key, "none")
            return username
        }

        fun logout(context: Context) {
            (context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
        }
    }

}