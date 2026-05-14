package org.arcanaforge.app.data.reading

import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.dao.ReadingDao
import org.arcanaforge.app.core.database.entity.ReadingEntity

interface ReadingRepository {
    fun observeRecentReadings(limit: Int): Flow<List<ReadingEntity>>
    fun observeReadingCount(): Flow<Int>
}

class OfflineReadingRepository(
    private val readingDao: ReadingDao,
) : ReadingRepository {
    override fun observeRecentReadings(limit: Int): Flow<List<ReadingEntity>> =
        readingDao.observeRecentReadings(limit)

    override fun observeReadingCount(): Flow<Int> = readingDao.observeReadingCount()
}
