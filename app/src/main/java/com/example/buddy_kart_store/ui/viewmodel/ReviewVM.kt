package com.example.buddy_kart_store.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddy_kart_store.model.repository.ReviewRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.Review

class ReviewVM(private val repo: ReviewRepo) : ViewModel() {

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> get() = _reviews

    val reviewResult = MutableLiveData<Pair<Boolean, String>>() // success + message

    // ✅ Loading state LiveData
    private val _isSubmitting = MutableLiveData<Boolean>()
    val isSubmitting: LiveData<Boolean> get() = _isSubmitting

    fun addReview(
        customerId: String,
        productId: String,
        name: String,
        rating: String,
        review: String
    ) {
        // ✅ Set loading state to true
        _isSubmitting.postValue(true)

        repo.addReview(customerId, productId, name, rating, review) { success, message ->
            // ✅ Update result
            reviewResult.postValue(Pair(success, message))
            // ✅ Reset loading state
            _isSubmitting.postValue(false)
        }
    }

    fun fetchReview(customerId: String, productId: String) {
        repo.fetchReview(customerId, productId) { list ->
            _reviews.postValue(list)
        }
    }

    fun getTotalReview(customerId: String, productId: String) {
        repo.getTotalReviews(customerId, productId)
    }
}
