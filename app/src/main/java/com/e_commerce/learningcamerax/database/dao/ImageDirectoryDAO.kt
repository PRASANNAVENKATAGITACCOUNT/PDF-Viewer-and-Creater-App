package com.e_commerce.learningcamerax.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.e_commerce.learningcamerax.database.ImageData

@Dao
interface ImageDirectoryDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImageData(imageData: ImageData)

    @Query("SELECT * FROM ImageData")
    fun getAllImageData(): LiveData<List<ImageData>>


    @Query("SELECT * FROM IMAGEDATA WHERE id=:imageDataId")
    fun getImageData(imageDataId:Long) : ImageData

}