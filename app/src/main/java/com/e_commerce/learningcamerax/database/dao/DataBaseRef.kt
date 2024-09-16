package com.e_commerce.learningcamerax.database.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.e_commerce.learningcamerax.database.ImageData

@Database(entities = [ImageData::class], version = 1)
abstract class DataBaseRef : RoomDatabase()  {
    abstract fun getDAO(): ImageDirectoryDAO

    companion object{
        private var INSTANCE:DataBaseRef?=null
        val DATABASE_NAME="IMAGE_DIRECTORY"

        fun getDatabaseRef(context: Context) : DataBaseRef{
            synchronized(this){
                var instance= INSTANCE
                if(instance==null){
                   instance = Room.databaseBuilder(context,
                       DataBaseRef::class.java,
                       DATABASE_NAME
                   ).fallbackToDestructiveMigration()
                       .build()
                    INSTANCE=instance
                }
                return instance
            }
        }

    }
}