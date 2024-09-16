package com.e_commerce.learningcamerax.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.e_commerce.learningcamerax.database.dao.ImageDirectoryDAO

class MainViewModelFactory(val dao: ImageDirectoryDAO) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainViewModel::class.java)){
            return MainViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")

    }
}