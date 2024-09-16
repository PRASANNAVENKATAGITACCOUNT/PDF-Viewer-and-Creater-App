package com.e_commerce.learningcamerax

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.e_commerce.learningcamerax.database.ImageData
import com.e_commerce.learningcamerax.database.dao.DataBaseRef
import com.e_commerce.learningcamerax.database.dao.ImageDirectoryDAO
import com.e_commerce.learningcamerax.databinding.ActivityMainBinding
import com.e_commerce.learningcamerax.image_files_section.ShowCreatedFiles
import com.e_commerce.learningcamerax.viewmodel.MainViewModel
import com.e_commerce.learningcamerax.viewmodel.MainViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit  var activityMainBinding:ActivityMainBinding

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var cameraProvider: ProcessCameraProvider

    lateinit var databaseRef: DataBaseRef

    lateinit var databaseDAO:ImageDirectoryDAO

    lateinit var mainViewModel: MainViewModel

    private var imageCapture:ImageCapture?=null

    private lateinit var imageAnalyzer : ImageAnalysis

    var cameraPosition = CameraSelector.DEFAULT_BACK_CAMERA

    var photosTaken:Int=0



    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        {
            permissions->
            var allPermissionGranted=true
            permissions.entries.forEach {
                if(it.key in REQUEST_PERMISSIONS && !it.value){
                    allPermissionGranted=false
                }
            }
            if(!allPermissionGranted){
                Toast.makeText(baseContext, "Permission Request Denied ",Toast.LENGTH_LONG).show()
            }else{
                startCamera()
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        databaseRef = DataBaseRef.getDatabaseRef(this)
        databaseDAO = databaseRef.getDAO()
        cameraExecutor = Executors.newSingleThreadExecutor()

        if(allPermissionGranted()){
           startCamera()
        }
        else{
            requestPermission()
        }

        val viewModelFactory=MainViewModelFactory(databaseDAO)

        mainViewModel = ViewModelProvider(this,viewModelFactory).get(MainViewModel::class.java)


        activityMainBinding.cameraPosition.setOnClickListener {
            cameraPosition = if(cameraPosition == CameraSelector.DEFAULT_BACK_CAMERA){
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }

        activityMainBinding.cameraButtonImage.setOnClickListener {
            photosTaken=++photosTaken
            activityMainBinding.countPhotoTaken.text="$photosTaken"
            takePhoto()

        }

        activityMainBinding.showAllData.setOnClickListener { view->
            try {
                val intent =
                    Intent(this@MainActivity.applicationContext, ShowCreatedFiles::class.java)
                this@MainActivity.startActivity(intent)
            }catch (e:Exception){
                Log.d("ErrorShowCreatedFiles ","${e.message}")
            }
        }

    }

    private fun allPermissionGranted() = REQUEST_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext,it)==PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider  = cameraProviderFuture.get();
            val preview = Preview
                .Builder()
                .build()
                .also {
                    it.setSurfaceProvider(activityMainBinding.preview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = cameraPosition

            imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor,LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }

            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture,imageAnalyzer)
            }catch (e:Exception){
                Log.e(TAG,"ErrorStartCamera Line try catch 89 : ",e)
            }

        },ContextCompat.getMainExecutor(this))

    }

    private fun takePhoto(){
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture
            .OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
            .build()


        activityMainBinding.cameraButtonImage.setBackgroundResource(R.drawable.take_photo_after_click)

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object :ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val msg = "Photo capture succeeded: ${outputFileResults.savedUri}"
                    val imageData = ImageData(0,name,outputFileResults.savedUri.toString())
                    mainViewModel.insertImageData(imageData)
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    activityMainBinding.cameraButtonImage.setBackgroundResource(R.drawable.take_photo_before_click)
                    Log.d(TAG, msg)

                }

                override fun onError(exception: ImageCaptureException) {

                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    activityMainBinding.cameraButtonImage.setBackgroundResource(R.drawable.take_photo_before_click)

                }

            }
        )
    }

    private fun requestPermission(){
        requestPermissionLauncher.launch(REQUEST_PERMISSIONS)
    }


    override fun onStart() {
        super.onStart()
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }



    override fun onDestroy() {
        super.onDestroy()
        cameraProvider.unbindAll()
        cameraExecutor.shutdown()
    }



    companion object {
        private const val TAG="CAMERA_X";
        private const val FILENAME_FORMAT="yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUEST_PERMISSIONS= mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU){
                add(android.Manifest.permission.READ_MEDIA_IMAGES)
            }else{
                add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }



}