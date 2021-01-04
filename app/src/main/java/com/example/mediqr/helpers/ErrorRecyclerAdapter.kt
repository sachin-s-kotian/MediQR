package com.example.mediqr.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediqr.R
import org.json.JSONArray

class ErrorRecyclerAdapter(private val errorList: JSONArray) :
    RecyclerView.Adapter<ErrorRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var message: TextView = view.findViewById(R.id.error_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.error_alert, parent, false)
        )
    }

    override fun getItemCount(): Int = errorList.length()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val error = errorList.getJSONObject(position)
        holder.message.text = error.getString("message")
    }
}