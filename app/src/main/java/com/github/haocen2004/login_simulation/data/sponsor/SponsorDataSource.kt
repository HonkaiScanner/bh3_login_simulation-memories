package com.github.haocen2004.login_simulation.data.sponsor

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.haocen2004.login_simulation.data.sponsor.database.SponsorDao
import com.github.haocen2004.login_simulation.data.sponsor.database.SponsorData
import com.github.haocen2004.login_simulation.utils.Logger

class SponsorDataSource(private val sponsorDao: SponsorDao) : PagingSource<Int, SponsorData>() {

    override fun getRefreshKey(state: PagingState<Int, SponsorData>): Int? {
        val key = state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
        Logger.d("load", "load key is $key")
        return key
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SponsorData> {
        val page = params.key ?: 0

        return try {
            Logger.d("load", "loading ${params.loadSize} to ${page * params.loadSize}")
            val entities = sponsorDao.getPagedList(params.loadSize, page * params.loadSize)
            LoadResult.Page(
                data = entities,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (entities.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}