package com.e_commerce.learningcamerax.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.e_commerce.learningcamerax.database.ImageData
import com.e_commerce.learningcamerax.database.dao.ImageDirectoryDAO
import com.e_commerce.learningcamerax.image_files_section.model.ImageUriData
import com.e_commerce.learningcamerax.image_files_section.model.PDFData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class MainViewModel(val dao: ImageDirectoryDAO) : ViewModel() {
    val job = Job()

    val uiScope = CoroutineScope(Dispatchers.Main+job)

    var listOfImageData : LiveData<List<ImageData>> = dao.getAllImageData() ?: MutableLiveData(mutableListOf())

    val listOfImageUriData : MutableList<ImageUriData> = mutableListOf()

    val selectedImagesUri : MutableList<Uri> = mutableListOf()

    val pdfFiles :MutableList<PDFData> = mutableListOf()


    init {
        Log.d("ListOfImageData ", listOfImageData.value.toString())
    }

    fun insertImageData(imageData: ImageData){
        uiScope.launch {
            insertData(imageData)
        }
    }

    // Logic 1
    fun saveToPdf(context: Context, outputStream: OutputStream?) : Flow<Float>{

        return flow {
            try {
                val pdfDocument = PdfDocument()
                selectedImagesUri.forEachIndexed { index, uri ->
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    val pageInfo =
                        PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)

                    val canvas = page.canvas
                    canvas.drawBitmap(bitmap, 0f, 0f, null)

                    pdfDocument.finishPage(page)
                    bitmap.recycle()
                    emit( ((index+1).toFloat()/selectedImagesUri.size) )
                }

                pdfDocument.writeTo(outputStream)

                pdfDocument.close()
            }
            catch (exception:Exception){
                Log.e("ErrorIn74","${exception.message}")
            }

        }
    }
    //Logic 2
    fun saveToPdf2(context: Context, outputStream: OutputStream?, getProgress:(Float)->Unit) {
            try {
                val pdfDocument = PdfDocument()
                selectedImagesUri.forEachIndexed { index, uri ->
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    val pageInfo =
                        PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)

                    val canvas = page.canvas
                    canvas.drawBitmap(bitmap, 0f, 0f, null)

                    pdfDocument.finishPage(page)
                    bitmap.recycle()
                    getProgress( ((index+1).toFloat()/selectedImagesUri.size) )
                }

                pdfDocument.writeTo(outputStream)

                pdfDocument.close()
            }
            catch (exception:Exception){
                Log.e("ErrorIn74","${exception.message}")
            }
    }


    private suspend fun insertData(imageData: ImageData){

        withContext(Dispatchers.IO){
            dao.insertImageData(imageData)
        }

    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}