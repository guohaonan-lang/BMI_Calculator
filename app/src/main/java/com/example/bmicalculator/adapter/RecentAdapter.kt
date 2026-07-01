package com.example.bmicalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bmicalculator.R
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.util.BmiUtil

class RecentAdapter(private var dataList: List<BmiEntity>) :
    RecyclerView.Adapter<RecentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var bmiText: TextView = itemView.findViewById<TextView>(R.id.item_recent_bmi)
        var timeText: TextView = itemView.findViewById<TextView>(R.id.item_recent_time)
        var bmiColor: View = itemView.findViewById<View>(R.id.item_recent_color)
        var bmiGrade: TextView = itemView.findViewById<TextView>(R.id.item_recent_grade)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentAdapter.ViewHolder, position: Int) {
        val item = dataList[position]
        val bmiInfo = BmiUtil.getBmiFullInfo(item.age, item.gender, item.bmiValue)

        holder.bmiText.text = item.bmiValue.toString()
        holder.timeText.text = item.timeText
        holder.bmiColor.backgroundTintList =
            android.content.res.ColorStateList.valueOf(bmiInfo.colorInt)
        holder.bmiGrade.text = bmiInfo.levelName
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    private var onItemClick: ((BmiEntity) -> Unit)? = null
    fun setOnItemClick(listener: ((BmiEntity) -> Unit)) {
        onItemClick = listener
    }

    override fun getItemCount(): Int = dataList.size

}