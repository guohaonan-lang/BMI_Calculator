package com.example.bmicalculator.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bmicalculator.R
import com.example.bmicalculator.model.Grade

class GradeAdapter(private var gradeList: List<Grade>) :
    RecyclerView.Adapter<GradeAdapter.ViewHolder>() {


    fun update(newList: List<Grade>) {
        gradeList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val circle: View = itemView.findViewById(R.id.item_grade_color)
        val grade: TextView = itemView.findViewById(R.id.item_grade_text)
        val range: TextView = itemView.findViewById(R.id.item_grade_scope)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grade, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val grade = gradeList[position]
        val ctx = holder.itemView.context

        if(grade.isSelect){
            val white = ctx.getColor(android.R.color.white)
            ViewCompat.setBackgroundTintList(
                holder.itemView as ConstraintLayout,
                ColorStateList.valueOf(grade.color)
            )
            // 圆点变白
            holder.circle.backgroundTintList = ColorStateList.valueOf(white)
            holder.grade.setTextColor(white)
            holder.range.setTextColor(white)
            holder.grade.alpha = 1f
            holder.range.alpha = 1f
            val boldFont = ResourcesCompat.getFont(ctx, R.font.font_extrabold)
            holder.grade.typeface = boldFont
            holder.range.typeface = boldFont
        }else{
            ViewCompat.setBackgroundTintList(
                holder.itemView as ConstraintLayout,
                null
            )
            holder.circle.backgroundTintList = ColorStateList.valueOf(grade.color)
            // 文字黑色、常规字体、半透明
            val black = ctx.getColor(R.color.black)
            holder.grade.setTextColor(black)
            holder.range.setTextColor(black)
            holder.grade.alpha = 0.7f
            holder.range.alpha = 0.7f
            val regularFont = ResourcesCompat.getFont(ctx, R.font.font_regular)
            holder.grade.typeface = regularFont
            holder.range.typeface = regularFont
        }
        holder.grade.text = grade.gradeName
        holder.range.text = grade.gradeRange
    }

    override fun getItemCount(): Int {
        return gradeList.size
    }

}