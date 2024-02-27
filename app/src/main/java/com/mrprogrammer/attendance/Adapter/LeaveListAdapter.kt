package com.mrprogrammer.attendance.Adapter


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.mrprogrammer.attendance.R
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder
import info.mrprogrammer.admin_bio.Model.LeaveDataMode

class LeaveListAdapter(private val context: Context, private val dataSet: List<LeaveDataMode>) :
    RecyclerView.Adapter<LeaveListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val roll: TextView = view.findViewById(R.id.rollNumber)
        val email: TextView = view.findViewById(R.id.email)
        val status: TextView = view.findViewById(R.id.status)
        val from: TextView = view.findViewById(R.id.from)
        val end: TextView = view.findViewById(R.id.end)
        val reason: TextView = view.findViewById(R.id.reason)
        val main: CardView = view.findViewById(R.id.main)
        val remakradmin: TextView = view.findViewById(R.id.remakradmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leave_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.roll.text = "Roll : "+dataSet[position].roll
        holder.email.text ="Email : "+ dataSet[position].email
        holder.from.text = "From Date and Time : "+dataSet[position].fromdate + " "+ dataSet[position].fromtime
        holder.end.text = "End Date and Time : "+dataSet[position].enddate + " "+ dataSet[position].endtime
        holder.reason.text = "Reasons : " +dataSet[position].reason
        holder.status.text = "Status : " +if(!dataSet[position].status) "Pending or declined" else "Approved"
        holder.remakradmin.text = dataSet[position].adminremark
    }



    override fun getItemCount() = dataSet.size
}
