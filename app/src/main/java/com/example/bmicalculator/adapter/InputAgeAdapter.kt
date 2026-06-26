package com.example.bmicalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bmicalculator.R
import com.example.bmicalculator.model.Age

class InputAgeAdapter(
    private val ageRecyclerView: RecyclerView,
    private val onAgeSelect: (Int) -> Unit
) : RecyclerView.Adapter<InputAgeAdapter.ViewHolder>() {


    private val ageList: List<Age> = getFixedAgeList()
    // 当前选中下标
    var selectedPosition = 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val age = itemView.findViewById<TextView>(R.id.item_age_text)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_age,parent,false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.age.text = ageList[position].num.toString()
        // 点击条目更新选中
        holder.itemView.setOnClickListener {
            updateSelectedPos(position)
            onAgeSelect(selectedPosition)
            scrollItemToCenter(ageRecyclerView, position)
        }
    }

    //将指定position滚动到屏幕中间
    fun scrollItemToCenter(rv: RecyclerView, targetPosition: Int) {
        val layoutManager = rv.layoutManager as LinearLayoutManager
        rv.post {
            val targetView = layoutManager.findViewByPosition(targetPosition) ?: return@post

            val itemWidth = targetView.width
            val rvWidth = rv.width
            val scrollDistance = targetView.left - (rvWidth - itemWidth) / 2

            rv.smoothScrollBy(scrollDistance, 0)
        }
    }

    fun updateSelectedPos(newPos: Int) {
        val oldPos = selectedPosition
        selectedPosition = newPos
        notifyItemChanged(oldPos)
        notifyItemChanged(newPos)
    }

    companion object {
        fun getFixedAgeList(): List<Age> {
            return (2..99).map { Age(it) }
        }
    }
    override fun getItemCount(): Int {
        return ageList.size

    }


}