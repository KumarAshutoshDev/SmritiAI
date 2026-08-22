package com.teamchromium.smritiai.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseTest {

    @Test
    fun writeReadDummyRecord() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = DatabaseProvider.getDatabase(context)

        val identityDao = db.identityDao()
        val behaviorDao = db.behaviorDao()

        val identityId = identityDao.insert(
            IdentityEntity(
                name = "Test Person",
                relationship = "Friend",
                faceEmbedding = FloatArray(4) { 1f }
            )
        )

        behaviorDao.insert(
            BehaviorEntity(
                contactId = identityId,
                photoRef = null,
                audioRef = null,
                aiSummary = null,
                transcript = "Hello, Smriti",
                timestamp = System.currentTimeMillis()
            )
        )

        val identities = identityDao.getAll()
        val behaviors = behaviorDao.getAll()

        assert(identities.any { it.name == "Test Person" })
        assert(behaviors.any { it.transcript == "Hello, Smriti" })
    }
}
