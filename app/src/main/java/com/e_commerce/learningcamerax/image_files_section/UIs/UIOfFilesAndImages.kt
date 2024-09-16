package com.e_commerce.learningcamerax.image_files_section.UIs

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.e_commerce.learningcamerax.R
import com.e_commerce.learningcamerax.image_files_section.PDFViewActivity
import com.e_commerce.learningcamerax.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Composable
fun ScreenFilesAndImages(viewModel: MainViewModel) {
    var saveDialogIndicator by remember {
        mutableStateOf(false)
    }

    val context=LocalContext.current

    Scaffold(
        floatingActionButton = {
            SaveToPdfButton(){
                saveDialogIndicator=!saveDialogIndicator
            }
        }
    ) {
        Column(Modifier.padding(it)) {
            Box {
                TabLayoutComposable(viewModel)
                if(saveDialogIndicator){
                    CreatingPdfDialog(
                        viewModel,
                        context,
                        onDismiss = { saveDialogIndicator= !saveDialogIndicator},
                    )
                }
            }
        }
    }

}


@Composable
fun TabLayoutComposable(viewModel: MainViewModel) {
    var position by remember {
        mutableStateOf(0)
    }
    val context=LocalContext.current

    Column (Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.15f),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = {
                position= 0
            } , Modifier.align(Alignment.CenterVertically)) {
                val color=if(position==0) Color.Blue else Color.Black
                Text(
                    text = " IMAGES ",
                    style = TextStyle(color = color,
                        fontSize = 20.sp))
            }

            TextButton(onClick = {
                position=1
            }, Modifier.align(Alignment.CenterVertically)) {
                val color=if(position==1) Color.Blue else Color.Black
                Text(text = " PDF'S",
                    style = TextStyle(color = color,
                        fontSize = 20.sp)
                )
            }

        }

        when(position){
            0->{
                ShowImages(viewModel,)
            }
            1->{
                ShowPDFs(viewModel){
                    pdfData->
                    val intent=Intent(context,PDFViewActivity::class.java)
                    intent.putExtra("path",pdfData.filePath)
                    context.startActivity(intent)
                }
            }
        }

    }

}

@Composable
fun SaveToPdfButton(onSaveClick:()->Unit) {
    ElevatedButton(
        onClick = {
            onSaveClick()
        },
        Modifier
            .size(width = 70.dp, height = 70.dp)
            .shadow(elevation = 8.dp, shape = CircleShape),
        colors = ButtonColors(
            contentColor = colorResource(id = R.color.floating_button_color),
            disabledContentColor =  colorResource(id = R.color.floating_button_color),
            containerColor = colorResource(id = R.color.floating_button_color),
            disabledContainerColor =  colorResource(id = R.color.floating_button_color)
        )
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.pdf_save_icon),
                contentDescription = " ",
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(25.dp)
            )
            Text(text = "Save PDF",
                style = TextStyle(fontSize = 10.sp) )
        }


    }
}



@Composable
fun CreatingPdfDialog(
    viewModel: MainViewModel,
    context: Context,
    onDismiss:()->Unit) {

    val scope = rememberCoroutineScope()

    val resolver = context.contentResolver

    var currentProgress by remember {
        mutableStateOf(0.0f)
    }

    AlertDialog(title = {
        LinearProgressIndicator(
            progress = {currentProgress},
            modifier = Modifier.height(20.dp)
        )
    },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val uri = getUriToSavePDF(context)
                        uri?.let {
                            resolver.openOutputStream(it).use { outputStream ->
                                if (viewModel.selectedImagesUri.isEmpty() || outputStream == null) {
                                    return@use
                                }
                                viewModel.saveToPdf(context, outputStream).collectLatest { progress ->
                                    currentProgress = progress
                                    Log.d("CurrentProgress", "${currentProgress}")
                                }
                            }
                        }
                        onDismiss()
                    }

                }
            ) {
                Text("YES")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text("NO")
            }
        }
    )


}





fun getUriToSavePDF(context : Context) : Uri?{
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME,"myfile${System.currentTimeMillis()}.pdf")
        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
    }
    val uri = resolver.insert(MediaStore.Files.getContentUri("external"),contentValues)
    return uri
}


@Preview(showBackground = true)
@Composable
fun PreviewMainUI(modifier: Modifier = Modifier) {
    Column(Modifier.fillMaxSize()) {
        CreatingPdfDialog(viewModel(), LocalContext.current, {})
    }

}



