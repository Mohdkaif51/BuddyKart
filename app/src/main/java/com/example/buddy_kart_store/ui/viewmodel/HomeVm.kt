package com.example.buddy_kart_store.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddy_kart_store.model.repository.HomeRepo
import com.example.buddy_kart_store.utlis.HomeModule
import kotlinx.coroutines.launch

class HomeVm(private val repo: HomeRepo) : ViewModel() {

    val homeModulesLiveData: LiveData<List<HomeModule>> = repo.homeModulesLiveData

    fun loadHome() {
        viewModelScope.launch {
            repo.fetchHomePage()
        }
    }


}

