package com.example.buddy_kart_store.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddy_kart_store.model.repository.RelatedProductRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedImage
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedProduct

class relatedProoductVM(private val repository: RelatedProductRepo) : ViewModel() {
    private val _relatedProducts = MutableLiveData<List<RelatedProduct>>()
    val relatedProducts: LiveData<List<RelatedProduct>> get() = _relatedProducts

    // ViewModel
    private val _relatedImages = MutableLiveData<List<RelatedImage>>()
    val relatedImages: LiveData<List<RelatedImage>> get() = _relatedImages

    fun fetchRelatedProducts(productId: String) {
        repository.fetchRelatedProducts(
            productId,
            onResult = { products ->
                _relatedProducts.postValue(products)  // update LiveData
            }
        )
    }

    private val _productImages = MutableLiveData<List<String>>() // List of image URLs
    val productImages: LiveData<List<String>> = _productImages



    fun fetchRelatedImages(productId: String) {
        repository.fetchRelatedImages(productId) { images ->
            _relatedImages.postValue(images)
        }
    }
}