package com.example.mediqr

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_scan.*

class ScanActivity : AppCompatActivity(), SurfaceHolder.Callback, Detector.Processor<Barcode> {

    private val _cam = 201
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private var isDetected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_scan)
    }

    override fun onStart() {
        super.onStart()
        when (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> run {
                this.barcodeDetector = BarcodeDetector.Builder(this).apply {
                    setBarcodeFormats(Barcode.QR_CODE)
                }.build()
                this.cameraSource = CameraSource.Builder(this, barcodeDetector).apply {
                    setAutoFocusEnabled(true)
                    setFacing(CameraSource.CAMERA_FACING_BACK)
                    setRequestedPreviewSize(1920, 1080)
                    setRequestedFps(12f)
                }.build()
                if (barcodeDetector.isOperational) {
                    barcodeDetector.setProcessor(this)
                }
                camera_surface.holder.addCallback(this)
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), _cam)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            _cam -> {
                Log.d("appx", grantResults[0].toString())
                when (grantResults.isNotEmpty()) {
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        finish()
                        startActivity(Intent(this, ScanActivity::class.java))
                    }
                    else -> finish()
                }
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        cameraSource.stop()
    }

    @SuppressLint("MissingPermission")
    override fun surfaceCreated(holder: SurfaceHolder?) {
        cameraSource.start(camera_surface.holder)
    }

    override fun receiveDetections(p0: Detector.Detections<Barcode>?) {
        p0?.detectedItems?.let {
            if (it.size() > 0 && !isDetected) {
                this.isDetected = true
                val intent = Intent(applicationContext, ViewerActivity::class.java).apply {
                    data = Uri.parse("mediqr://view/${it.valueAt(0).rawValue}")
                }
                startActivity(intent)
                finish()
                ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(
                    ToneGenerator.TONE_CDMA_PIP,
                    100
                )
            }
        }
    }

    override fun release() {
        Toast.makeText(this, "Resources busy", Toast.LENGTH_LONG).show()
    }
}
