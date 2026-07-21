package com.example.bmicalculator.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.widget.TextView
import com.example.bmicalculator.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class BmiMarkerView(context: Context) : MarkerView(context, R.layout.marker_pop) {
    private val markerText: TextView = findViewById(R.id.marker_text)
    private val markerCircle: View = findViewById(R.id.marker_circle)

    @SuppressLint("DefaultLocale")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        e ?: return
        val yVal = e.y
        val circleColor = BmiUtil.getBmiFullInfo(context, 25, 1, yVal).colorInt
        val layerDrawable = markerCircle.background as LayerDrawable
        val innerCircle = layerDrawable.findDrawableByLayerId(R.id.inner_circle)
        innerCircle.setTint(context.getColor(circleColor))
        markerText.text = String.format("%.1f", yVal)
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
//        return super.getOffset()
        return MPPointF(-(width / 2f), 18f - height.toFloat())
    }
}