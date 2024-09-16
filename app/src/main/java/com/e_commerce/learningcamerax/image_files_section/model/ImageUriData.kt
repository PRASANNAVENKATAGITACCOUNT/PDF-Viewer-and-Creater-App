package com.e_commerce.learningcamerax.image_files_section.model

import android.net.Uri
import java.util.UUID

data class ImageUriData(
    val imageUri:Uri,
    var isSelected:Boolean = false,
    val id:String = UUID.randomUUID().toString())
