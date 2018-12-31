package com.bodybank.demo

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.bodybank.ui.camera.CameraFragment

class MainActivity : AppCompatActivity() {

    companion object {
        val PERMISSION_REQUEST_CAMERA = 111
    }

    var callbackAfterPermissionGranted: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
        supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, CameraFragment())
            ?.commit()
        checkPermission {
            supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, CameraFragment())
                ?.commit()

        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager?.backStackEntryCount ?: 0 > 0) {
            supportFragmentManager?.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    @TargetApi(23)
    internal fun checkPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback()
        } else {
            callbackAfterPermissionGranted = callback
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                AlertDialog.Builder(this).setMessage("Camera permission is requested to take photo.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            PERMISSION_REQUEST_CAMERA
                        )
                    }.setNegativeButton("Cancel") { _, _ -> }.show()
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_CAMERA
                )
            }
        }
    }

    @TargetApi(23)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            var granted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            if (granted) {
                callbackAfterPermissionGranted?.let {
                    it()
                }
            }
        }
    }
}
