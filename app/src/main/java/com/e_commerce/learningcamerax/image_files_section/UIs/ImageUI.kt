package com.e_commerce.learningcamerax.image_files_section.UIs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.e_commerce.learningcamerax.image_files_section.model.ImageUriData
import com.e_commerce.learningcamerax.viewmodel.MainViewModel


@Composable
fun ShowImages(viewModel: MainViewModel) {
    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        items(
            viewModel.listOfImageUriData,
            key={
                it.id
            }
        ){ imageUriData->
            EachImage(imageUriData)
            { imageUriData->
                if(imageUriData.isSelected){
                    viewModel.selectedImagesUri.add(imageUriData.imageUri)
                }else{
                    viewModel.selectedImagesUri.remove(imageUriData.imageUri)
                }
            }
        }
    }
}


@Composable
fun EachImage(imageUriData: ImageUriData , addRemoveSelectedImage:(ImageUriData)->Unit) {

    var selectedImage by remember {
        mutableStateOf(imageUriData.isSelected)
    }

    var imageLoading by remember {
        mutableStateOf(true)
    }

    Box (
            Modifier.fillMaxSize()
    ){
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUriData.imageUri)
                    .build()
                , contentDescription = " ",

                Modifier
                    .padding(4.dp)
                    .clip(RectangleShape),

                onSuccess = {
                    imageLoading=false
                }
            )

            Checkbox(
                checked = selectedImage,
                onCheckedChange =
                {
                selectedImage=!selectedImage
                imageUriData.isSelected=selectedImage
                    addRemoveSelectedImage(imageUriData)
                },
                Modifier.align( Alignment.BottomEnd) )
        }

        if(imageLoading){
            RectangleShimmer()
        }
    }

}
