package com.androidmads.ocrcamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.Text
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer

import java.io.IOException

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback,  Detector.Processor<TextBlock> {

    private var cameraView: SurfaceView? = null
    private var txtView: TextView? = null
    private var cameraSource: CameraSource? = null

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource!!.start(cameraView!!.holder)
                    } catch (e: Exception) { }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraView = findViewById(R.id.surface_view)
        txtView = findViewById(R.id.txtview)
        val txtRecognizer = TextRecognizer.Builder(applicationContext).build()
        if (!txtRecognizer.isOperational) {
            Log.e("Main Activity", "Detector dependencies are not yet available")
        } else {
            cameraSource = CameraSource.Builder(applicationContext, txtRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build()
            cameraView!!.holder.addCallback(this)
            txtRecognizer.setProcessor(this)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
                return
            }
            cameraSource!!.start(cameraView!!.holder)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        cameraSource!!.stop()
    }

    override fun release() {
    }

    override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
        val items = detections!!.detectedItems
        val strBuilder = StringBuilder()


        for (i in 0 until items.size()) {
            val item = items.valueAt(i) as TextBlock
            strBuilder.append(item.value)
            /* The following Process is used to show how to use lines & elements as well
            for (j in 0 until items.size()) {
                val textBlock = items.valueAt(j) as TextBlock
                strBuilder.append(textBlock.value)
                strBuilder.append("/")
                for (line in textBlock.components) {
                    //extract scanned text lines here
                    Log.v("lines", line.value)
                    strBuilder.append(line.value)
                    strBuilder.append("/")
                    for (element in line.components) {
                        //extract scanned text words here
                        Log.v("element", element.value)
                        strBuilder.append(element.value)
                    }
                }
            }*/
        }
        Log.v("strBuilder.toString()", strBuilder.toString())
        txtView!!.post {
            txtView!!.text = strBuilder.toString()
            //Toast.makeText(this, strBuilder.toString(), Toast.LENGTH_SHORT)
            //Toast.makeText(this, items.toString(), Toast.LENGTH_SHORT)
        }
    }
}
