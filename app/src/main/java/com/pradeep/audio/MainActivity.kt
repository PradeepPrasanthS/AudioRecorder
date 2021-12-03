package com.pradeep.audio

import android.Manifest
import android.content.ContentValues
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var mr: MediaRecorder
    private var audioURI: Uri? = null
    private var file: ParcelFileDescriptor? = null
    private var recordingStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mr = MediaRecorder()

        Util.requestPermission(this, Manifest.permission.RECORD_AUDIO)
        Util.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        startRecord.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (recordingStarted) {
                    mr.resume()
                    startRecord.visibility = View.GONE
                    stopRecord.visibility = View.VISIBLE
                    cancelRecord.visibility = View.VISIBLE
                    saveRecord.visibility = View.VISIBLE
                } else {
                    val values = ContentValues(4)
                    values.put(MediaStore.Audio.Media.TITLE, "record")
                    values.put(
                        MediaStore.Audio.Media.DATE_ADDED,
                        (System.currentTimeMillis() / 1000).toInt()
                    )
                    values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Recordings/")

                    audioURI = contentResolver.insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                    audioURI?.let {
                        file = contentResolver.openFileDescriptor(it, "w")!!
                    }

                    file?.let {
                        mr.setAudioSource(MediaRecorder.AudioSource.MIC)
                        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        mr.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
                        mr.setOutputFile(it.fileDescriptor)
                        mr.setAudioChannels(1)
                        mr.prepare()
                        mr.start()
                        startRecord.visibility = View.GONE
                        stopRecord.visibility = View.VISIBLE
                        cancelRecord.visibility = View.VISIBLE
                        saveRecord.visibility = View.VISIBLE
                        recordingStarted = true
                    }
                }
            }
        }

        stopRecord.setOnClickListener {
            mr.pause()
            startRecord.visibility = View.VISIBLE
            stopRecord.visibility = View.GONE
            cancelRecord.visibility = View.GONE
            saveRecord.visibility = View.GONE
        }

        saveRecord.setOnClickListener {
            mr.stop()
            val mp = MediaPlayer()
            file?.let {
                mp.setDataSource(it.fileDescriptor)
                mp.prepare()
                mp.start()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}