package com.hf.sportsscorekeeper

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.database.Cursor
import kotlinx.android.synthetic.main.gamerecord_item.view.*

class RecordViewAdapter(db_sessionID:Array<Int>, db_dateTime:Array<String>, db_finalWinCount:Array<Array<Int>>): RecyclerView.Adapter<CustomViewHolder>() {
    // Get number of items
    val list_sessionID      = db_sessionID
    val list_dateTime       = db_dateTime
    val list_finalWinCount  = db_finalWinCount

    override fun getItemCount(): Int {
        //return array_records.size
        return list_sessionID.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        // Creating a view
        val layoutInflater = LayoutInflater.from(parent?.context)
        val cellForRow = layoutInflater.inflate(R.layout.gamerecord_item, parent, false)
        return CustomViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.view.lbl_sessionID.text      = "Session ID: " + list_sessionID[position].toString()
        holder.view.lbl_dateTime.text       = list_dateTime[position]

        holder.view.lbl_scorePlayer1.text   = list_finalWinCount[position][0].toString()
        holder.view.lbl_scorePlayer2.text   = list_finalWinCount[position][1].toString()
    }
}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view) {

}