package com.pradeep.audio

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RecordingsActivity : AppCompatActivity() {

    lateinit var recordings: MutableList<Audio>

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings)

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_TAKEN
        )
        val selection = "${MediaStore.Audio.Media.DATE_TAKEN} >= ?"

        val selectionArgs = arrayOf(
            dateToTimestamp(day = 1, month = 1, year = 2020).toString()
        )

        val sortOrder = "${MediaStore.Audio.Media.DATE_TAKEN} DESC"

        application.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            recordings = addRecordingsFromCursor(cursor)
        }

        val recordingAdapter = RecordingsAdapter(this, recordings)

        val recyclerView: RecyclerView = findViewById(R.id.audio_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recordingAdapter

    }

    @Suppress("SameParameterValue")
    @SuppressLint("SimpleDateFormat")
    private fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
        SimpleDateFormat("dd.MM.yyyy").let { formatter ->
            formatter.parse("$day.$month.$year")?.time ?: 0
        }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun addRecordingsFromCursor(cursor: Cursor): MutableList<Audio> {
        val audios = mutableListOf<Audio>()


        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_TAKEN)
        val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

        while (cursor.moveToNext()) {

            val id = cursor.getLong(idColumn)
            val dateTaken = Date(cursor.getLong(dateTakenColumn))
            val displayName = cursor.getString(displayNameColumn)

            val contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id
            )

            val audio = Audio(id, displayName, dateTaken, contentUri)
            audios += audio

        }
        return audios
    }
}