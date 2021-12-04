package com.pradeep.audio

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    lateinit var mr: MediaRecorder
    lateinit var timer: CountDownTimer
    private var audioURI: Uri? = null
    private var file: ParcelFileDescriptor? = null
    private var recordingStarted = false
    private var count: Long = 0
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mr = MediaRecorder()

        Util.requestPermission(this, Manifest.permission.RECORD_AUDIO)
        Util.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
            if (it.resultCode == RESULT_OK){
                Toast.makeText(this@MainActivity, "Audio deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Audio deletion failed", Toast.LENGTH_SHORT).show()
            }
        }

        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                count += 60
                val millis: Long = count
                var seconds = (millis / 60).toInt()
                val minutes = seconds / 60
                seconds %= 60
                timer_id.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {}
        }

        startRecord.setOnClickListener {
            startRecording()
        }

        stopRecord.setOnClickListener {
            stopRecord()
        }

        resumeRecord.setOnClickListener {
            resumeRecord()
        }

        cancelRecord.setOnClickListener {
            cancelRecord()
        }

        saveRecord.setOnClickListener {
            saveRecord()
        }
    }

    private fun stopRecord() {
        timer.cancel()
        mr.pause()
        stopRecord.visibility = View.GONE
        resumeRecord.visibility = View.VISIBLE
    }

    private fun resumeRecord() {
        timer.start()
        mr.resume()
        stopRecord.visibility = View.VISIBLE
        resumeRecord.visibility = View.GONE
    }

    private fun saveRecord() {
        val saveAudioDialog = SaveAudioDialog(this@MainActivity, audioURI, file)
        saveAudioDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        saveAudioDialog.show()
        timer.cancel()
        mr.stop()
        startRecord.visibility = View.VISIBLE
        stopRecord.visibility = View.GONE
        cancelRecord.visibility = View.GONE
        saveRecord.visibility = View.GONE
        resumeRecord.visibility = View.GONE
        timer_id.text = "00:00"
        recordingStarted = false
    }

    private fun cancelRecord(){
        timer.cancel()
        mr.stop()
        CoroutineScope(Dispatchers.IO).launch {
            deleteAudio()
        }
        startRecord.visibility = View.VISIBLE
        stopRecord.visibility = View.GONE
        cancelRecord.visibility = View.GONE
        saveRecord.visibility = View.GONE
        resumeRecord.visibility = View.GONE
        timer_id.text = "00:00"
        recordingStarted = false
    }

    private suspend fun deleteAudio(){
        withContext(Dispatchers.IO){
            try {
                audioURI?.let { contentResolver.delete(it, null, null) }
            } catch (e: SecurityException){
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(contentResolver, listOf(audioURI)).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }
                intentSender?.let { sender ->
                    intentSenderLauncher.launch(
                            IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }

    private fun startRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (recordingStarted) {
                mr.resume()
                timer.start()
                startRecord.visibility = View.GONE
                stopRecord.visibility = View.VISIBLE
                cancelRecord.visibility = View.VISIBLE
                saveRecord.visibility = View.VISIBLE
            } else {
                count = 0
                timer.start()
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

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startRecord.isClickable = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.recording -> {
                val intent = Intent(this@MainActivity, RecordingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}