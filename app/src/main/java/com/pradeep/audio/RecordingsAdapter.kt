package com.pradeep.audio

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

class RecordingsAdapter(private val activity: RecordingsActivity, private val dataSet: MutableList<Audio>) :
    RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileName)
        val date: TextView = view.findViewById(R.id.date)
        val playAudio: ImageView = view.findViewById(R.id.playAudio)
        val pauseAudio: ImageView = view.findViewById(R.id.pauseAudio)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recording_cell, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mp = MediaPlayer()
        try {
            mp.setDataSource(activity, dataSet[position].contentUri)
            mp.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        viewHolder.fileName.text = dataSet[position].displayName
        viewHolder.date.text = dataSet[position].dateTaken.toString()
        viewHolder.playAudio.setOnClickListener {
            viewHolder.pauseAudio.visibility = View.VISIBLE
            mp.start()
        }
        viewHolder.pauseAudio.setOnClickListener {
            viewHolder.pauseAudio.visibility = View.GONE
            viewHolder.playAudio.visibility = View.VISIBLE
            mp.pause()
        }
    }

    override fun getItemCount() = dataSet.size

}
