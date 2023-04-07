package com.github.haocen2004.login_simulation.data.sponsor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.haocen2004.login_simulation.data.sponsor.database.SponsorDao

class SponsorViewModelFactory(
    private val dao: SponsorDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SponsorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SponsorViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}