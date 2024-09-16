package com.e_commerce.learningcamerax.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ImageData(
    @PrimaryKey(autoGenerate = true)
    var id:Long,
    var imageName:String,
    var imagePath:String)

data class Docs(var fileName:String, var filePath:String)

data class Images(var imageName:String, var imagePath:String)