package com.mrprogrammer.attendance.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mrprogrammer.attendance.DataUtils
import com.mrprogrammer.attendance.R
import com.mrprogrammer.attendance.Respositoy.CommonRepo
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder
import org.w3c.dom.Text
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale


class Dashboard : Fragment() {
    private var totalLeave:TextView? = null
    private var totalDays:TextView? = null
    private var present:TextView? = null
    private var odLeave:TextView? = null
    private var percentage:TextView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dashboard_fragment, container, false)
        totalLeave = view?.findViewById<TextView>(R.id.total_leave)
        totalDays = view?.findViewById<TextView>(R.id.totalDays)
        present = view?.findViewById<TextView>(R.id.present)
        odLeave = view?.findViewById<TextView>(R.id.od)
        percentage = view?.findViewById<TextView>(R.id.percentage)
        initValue()
        DataUtils.updateDataToUi(requireContext())
        return view
    }

    private fun initValue() {
        CommonRepo.getLeave().observeForever {
            totalLeave?.text = it["LEAVE"].toString()
            totalLeave?.text = it["PERMISSION"].toString()
            odLeave?.text = it["ON DUTY"].toString()
        }

        ObjectHolder.getInstance()?.getAttdanceModel()?.observeForever {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val date1 = LocalDate.parse(it.startDate, formatter)
            val date2 = LocalDate.parse(it.endDate, formatter)
            val difference = ChronoUnit.DAYS.between(date1, date2)
            totalDays?.text =  difference.toString()
        }
    }
}
