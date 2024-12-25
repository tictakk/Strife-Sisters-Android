package com.laconic.util

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.laconic.strifesdroid.R

class RecyclerAdapter(private val dataSet: Array<String>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){
    private var clickListener: OnClickListener? = null

        class ViewHolder(view: View): RecyclerView.ViewHolder(view){
            val textView: TextView = view.findViewById(R.id.textView)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_row_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { view ->
            clickListener?.onClick(position)
        }
        holder.textView.text = dataSet[position]
    }

    fun setOnClickListener(listener: OnClickListener?){
        clickListener = listener
    }

    public interface OnClickListener{
        fun onClick(position: Int)
    }

    override fun getItemCount() = dataSet.size

}