package com.e_commerce.learningcamerax.image_files_section.UIs

import android.content.Intent
import android.widget.ImageButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.e_commerce.learningcamerax.R
import com.e_commerce.learningcamerax.image_files_section.PDFViewActivity
import com.e_commerce.learningcamerax.image_files_section.model.PDFData
import com.e_commerce.learningcamerax.viewmodel.MainViewModel

@Composable
fun ShowPDFs(
    viewModel: MainViewModel,
    onPDFItemClick : (PDFData)->Unit
) {
    Column (
        Modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(viewModel.pdfFiles){ pdfItem->
                PDFItem(pdfItem){ pdfItem->
                       onPDFItemClick(pdfItem)
                }
            }
        }
    }
}


@Composable
fun PDFItem(pdfItem: PDFData, showPDF:(pdfData:PDFData)->Unit) {
    Row (
        Modifier
            .padding(10.dp)
            .clickable {
                showPDF(pdfItem)
            }
    ){
        Image(painter = painterResource(id = R.drawable.pdf_icon),
            contentDescription =" PDF Icons ",
            modifier = Modifier
                .width(25.dp)
                .height(20.dp))

        Text(text = pdfItem.fileName,
            style = TextStyle(fontSize = 15.sp))
    }
}
