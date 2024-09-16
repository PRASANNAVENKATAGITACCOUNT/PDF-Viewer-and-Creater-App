package com.e_commerce.learningcamerax.image_files_section

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.e_commerce.learningcamerax.R
import com.e_commerce.learningcamerax.databinding.ActivityPdfviewBinding
import java.io.File

class PDFViewActivity : AppCompatActivity() {

    lateinit  var pdfviewBinding: ActivityPdfviewBinding
    lateinit var prevButton : ImageView
    lateinit var nextButton : ImageView
    
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private var currentPage: PdfRenderer.Page? = null

    private var pageIndex: Int = 0
    lateinit var imagePDF:ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pdfviewBinding = ActivityPdfviewBinding.inflate(layoutInflater)
        setContentView(pdfviewBinding.root)
        imagePDF = pdfviewBinding.pdfImageView
        prevButton = pdfviewBinding.prevButton
        nextButton = pdfviewBinding.nextButton

        try {
            val path = intent.getStringExtra("path")
            val pdfFile = File(path)
            parcelFileDescriptor =
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            renderPage(imagePDF, pageIndex)
        }catch (e:Exception){
            Log.e("ErrorIn46","${e.message}")
        }

        prevButton.setOnClickListener {
            if (pageIndex > 0) {
                pageIndex--
                renderPage(imagePDF, pageIndex)
            }
        }

        nextButton.setOnClickListener {
            if (pageIndex < pdfRenderer.pageCount - 1) {
                pageIndex++
                renderPage(imagePDF, pageIndex)
            }
        }
    }

    private fun renderPage(pdfImageView: ImageView, index: Int) {
        currentPage?.close()
        currentPage = pdfRenderer.openPage(index)
        val bitmap = Bitmap.createBitmap(
            currentPage!!.width, currentPage!!.height,
            Bitmap.Config.ARGB_8888
        )
        currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        pdfImageView.setImageBitmap(bitmap)
    }
}