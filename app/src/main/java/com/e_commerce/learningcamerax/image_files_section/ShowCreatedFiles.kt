package com.e_commerce.learningcamerax.image_files_section

import android.content.ContentUris
import com.e_commerce.learningcamerax.image_files_section.UIs.ScreenFilesAndImages
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.e_commerce.learningcamerax.database.dao.DataBaseRef
import com.e_commerce.learningcamerax.databinding.ActivityShowCreatedFilesBinding
import com.e_commerce.learningcamerax.image_files_section.model.ImageUriData
import com.e_commerce.learningcamerax.image_files_section.model.PDFData
import com.e_commerce.learningcamerax.viewmodel.MainViewModel
import com.e_commerce.learningcamerax.viewmodel.MainViewModelFactory



class ShowCreatedFiles : AppCompatActivity() {

    lateinit var databaseRef: DataBaseRef
    lateinit var viewModelFactory: MainViewModelFactory
    lateinit var viewModel: MainViewModel

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        result->

        if(hasManageExternalStoragePermission()){
            getAllPDF()
        }else{
            showToast()
        }

    }

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { isEnabled ->
            var allPermissionGranted = true
            isEnabled.forEach { (permission, result) ->
                if(!result){
                    allPermissionGranted = false
                }
            }

            if(allPermissionGranted){
                fetchAllImages()
            }else{
                showToast()
            }

        }

    private lateinit var binding : ActivityShowCreatedFilesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowCreatedFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = DataBaseRef.getDatabaseRef(this)
        viewModelFactory = MainViewModelFactory(dao = databaseRef.getDAO())
        viewModel = ViewModelProvider(this,viewModelFactory).get(MainViewModel::class.java)

        if(areAllPermissionGranted() && hasManageExternalStoragePermission()){
            fetchAllImages()
            getAllPDF()
            Log.d("ListOfImages","${viewModel.listOfImageUriData} ")
        }else{
            askReadWritePermission()
            askManageExternalStorage()
        }

        binding.comp.setContent {
            ScreenFilesAndImages(viewModel)
        }

    }



    private fun askReadWritePermission() {
        requestPermissionLauncher.launch(permissions)
    }

    private fun areAllPermissionGranted() : Boolean{
        var allPermissionGranted=true
        for(permission in permissions){
            if(ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
                allPermissionGranted=false;
                break;
            }
        }
        return allPermissionGranted
    }

    private fun hasManageExternalStoragePermission() :Boolean=Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()


    private fun fetchAllImages() {
        getAllImages(this)
    }
    private fun askManageExternalStorage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            resultLauncher.launch(intent)
        }
    }

    private fun showToast() {
        Toast.makeText(this,"Permission not granted",Toast.LENGTH_LONG).show()
    }




    private fun getAllImages(context:Context){
        try{
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val query = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            if(query == null){
                Toast.makeText(this,"Query is null ",Toast.LENGTH_LONG).show()
            }else{
                query.use { cursor->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    while (cursor.moveToNext()){
                        val id = cursor.getLong(idCol)
                        val contentUri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id.toString()
                        )
                        viewModel.listOfImageUriData.add(ImageUriData(contentUri,false))
                    }
                }
            }

        }catch (e:Exception){
            Log.d("ErrorShowCreatedFiles ","${e.message}")
        }

    }


    private fun getAllPDF(){
        val uri : Uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )
        val selection ="${MediaStore.Files.FileColumns.MIME_TYPE}=?"
        val selectionArgs= arrayOf("application/pdf")
        val cursor = contentResolver.query(uri,projection,selection,selectionArgs,null)
        cursor?.use {
            val idColumn=it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val displayNameColumn  = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            
            while (it.moveToNext()){
                val field = it.getLong(idColumn)
                val filePath = it.getString(dataColumn)
                val displayName  = it.getString(displayNameColumn)
                val fileUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"),field)
                viewModel.pdfFiles.add(PDFData(fileUri,displayName,filePath))
            }

        }
    }


    companion object{
        val permissions = mutableListOf<String>().apply {
            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU){
                add(android.Manifest.permission.READ_MEDIA_IMAGES)
            }else{
                add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }



}