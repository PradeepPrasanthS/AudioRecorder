package com.pradeep.audio

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.Window
import android.widget.Button
import android.widget.TextView


class SaveAudioDialog(
    private val c: Activity,
    private var audioURI: Uri?,
    private var file: ParcelFileDescriptor?
    ) : Dialog(c) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.add_filename_dialog)
        val addAudio: Button = findViewById(R.id.addAudio)
        val fileNameAudio: TextView = findViewById(R.id.fileNameAudio)
        addAudio.setOnClickListener {
            val values = ContentValues(1)
            values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileNameAudio.text.toString())

            c.finish()
        }
    }
}