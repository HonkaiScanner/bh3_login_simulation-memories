package com.github.haocen2004.login_simulation.data.sponsor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.github.haocen2004.login_simulation.data.sponsor.database.SponsorDao

class SponsorViewModel(
    private val dao: SponsorDao
) : ViewModel() {

    val data = Pager(
        PagingConfig(
            pageSize = 8
        ),
    ) {
        SponsorDataSource(dao)
    }.flow.cachedIn(viewModelScope)

}

