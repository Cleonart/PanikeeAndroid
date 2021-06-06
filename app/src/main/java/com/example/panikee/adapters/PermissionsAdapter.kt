package com.example.panikee.adapters

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsAdapter {

    fun check(act: Activity, ctx: Context): Boolean{
        if (checkPermissionFromDevice(ctx)){
            return true;
        }
        val permissions = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.INTERNET)
        ActivityCompat.requestPermissions(act, permissions, 0)
        return false;
    }

    private fun checkPermissionFromDevice(ctx:Context): Boolean {
        val writeExternalStorageResult:Int = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val recordAudioResult = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.RECORD_AUDIO)
        val sendSMS = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.SEND_SMS)
        val internet = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.INTERNET)
        return  writeExternalStorageResult == PackageManager.PERMISSION_GRANTED &&
                recordAudioResult == PackageManager.PERMISSION_GRANTED &&
                sendSMS == PackageManager.PERMISSION_GRANTED &&
                internet == PackageManager.PERMISSION_GRANTED
    }

}