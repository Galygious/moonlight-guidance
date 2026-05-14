package org.arcanaforge.app.data.layout

import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.dao.LayoutDao
import org.arcanaforge.app.core.database.entity.LayoutEntity

interface LayoutRepository {
    fun observeLayouts(): Flow<List<LayoutEntity>>
}

class OfflineLayoutRepository(
    private val layoutDao: LayoutDao,
) : LayoutRepository {
    override fun observeLayouts(): Flow<List<LayoutEntity>> = layoutDao.observeLayouts()
}
