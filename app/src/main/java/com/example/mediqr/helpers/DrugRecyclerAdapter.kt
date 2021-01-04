package com.example.mediqr.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediqr.R
import org.json.JSONArray

class DrugRecyclerAdapter(private val drugs: JSONArray) :
    RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.drug_row, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return drugs.length()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        drugs.getJSONObject(position).also {
            holder.name.text = it.getString("drug")
            holder.notes.text = it.getString("notes")
        }
    }

}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var name: TextView = view.findViewById(R.id.drug_title)
    var notes: TextView = view.findViewById(R.id.drug_notes)
}