package com.pradeep.audio

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class Util {
    companion object{
        fun requestPermission(activity: Activity?, permission: String) {
            if (ContextCompat.checkSelfPermission(activity!!, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), 0)
            }
        }
    }
}