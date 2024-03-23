package com.mrprogrammer.attendance.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mrprogrammer.attendance.DataUtils
import com.mrprogrammer.attendance.R
import com.mrprogrammer.attendance.Respositoy.CommonRepo
import com.mrprogrammer.attendance.Utils
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
    private var permission:TextView? = null
    private var present:TextView? = null
    private var odLeave:TextView? = null
    private var percentage:TextView? = null
    private var webView:WebView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dashboard_fragment, container, false)
        totalLeave = view?.findViewById<TextView>(R.id.total_leave)
        permission = view?.findViewById<TextView>(R.id.permission)
        totalDays = view?.findViewById<TextView>(R.id.totalDays)
        present = view?.findViewById<TextView>(R.id.present)
        webView = view?.findViewById<WebView>(R.id.webView)
        odLeave = view?.findViewById<TextView>(R.id.od)
        percentage = view?.findViewById<TextView>(R.id.percentage)
        initValue()
        DataUtils.updateDataToUi(requireContext())
        initValeLoadUrl()
        return view
    }


    private fun initValeLoadUrl() {
       webView?.settings?.javaScriptEnabled = true

        webView?.webViewClient = WebViewClient()

        webView?.webChromeClient = WebChromeClient()

        val newDate = Utils.getNewsDate()
        webView?.loadUrl("https://bitsathy.aflip.in/bitsathy_daily_news_${newDate}.html")

    }

    fun calculatePercentage(totalDays: Int, daysPresent: Int): Double {
        return if (totalDays != 0) {
            (daysPresent.toDouble() / totalDays.toDouble()) * 100
        } else {
            0.0
        }
    }



    private fun initValue() {
        CommonRepo.getLeave().observeForever {
            totalLeave?.text = it["LEAVE"].toString()
            permission?.text = it["PERMISSION"].toString()
            odLeave?.text = it["ON DUTY"].toString()



            try {
                val total:Int = it["LEAVE"]!!.toInt() + it["PERMISSION"]!!.toInt() + it["ON DUTY"]!!.toInt()
                val totalDay = totalDays?.text.toString().toInt()
                present?.text = (totalDay - total).toString()
                val percentage1 = calculatePercentage(totalDay, (totalDay - total))
                percentage?.text = percentage1.toString()

           } catch (e:Exception) {
               e.printStackTrace()
           }

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
