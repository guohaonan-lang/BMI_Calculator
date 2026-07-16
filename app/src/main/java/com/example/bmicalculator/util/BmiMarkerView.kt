package com.example.bmicalculator.util

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.example.bmicalculator.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class BmiMarkerView(context: Context) : MarkerView(context, R.layout.marker_pop) {
    private val markerText: TextView = findViewById(R.id.marker_text)

    @SuppressLint("DefaultLocale")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e ?:return
        val yVal = e.y
        markerText.text = String.format("%.1f", yVal)
        super.refreshContent(e, highlight)

    }

    override fun getOffset(): MPPointF {
//        return super.getOffset()
        return MPPointF(-(width / 2f), -height.toFloat() - 8f)
    }
}