package org.arcanaforge.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun seedDataCreatesSampleDeckAndLayouts() = runTest {
        DatabaseSeeder(database).seedIfNeeded()

        assertEquals(1, database.deckDao().countDecks())
        assertTrue(database.layoutDao().countLayoutById("layout-one-card") > 0)
        assertTrue(database.layoutDao().countLayoutById("layout-three-card-past-present-future") > 0)
        assertTrue(database.layoutDao().countLayoutById("layout-seven-chakra-check-in") > 0)
    }
}
