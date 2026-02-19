package com.tucugo.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tucugo.app.data.models.Business
import com.tucugo.app.data.repository.BusinessRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardViewModel(
    repository: BusinessRepository = BusinessRepository()
) : ViewModel() {

    private val _businesses = MutableLiveData<List<Business>>()
    val businesses: LiveData<List<Business>> = _businesses

    private val _categories = MutableLiveData<List<CategoryItem>>()
    val categories: LiveData<List<CategoryItem>> = _categories

    init {
        loadCategories()
        
        // Optimizamos: Usamos el repositorio directamente en el init
        // Esto permite remover el 'val' del constructor y ahorrar memoria.
        viewModelScope.launch {
            repository.getFeaturedBusinesses().collectLatest { businessList ->
                _businesses.value = businessList
            }
        }
    }

    private fun loadCategories() {
        _categories.value = listOf(
            CategoryItem("Restaurantes", "restaurante"),
            CategoryItem("Supermercados", "supermercado"),
            CategoryItem("Farmacias", "farmacia"),
            CategoryItem("Otros", "otros")
        )
    }

    data class CategoryItem(val name: String, val id: String)
}
