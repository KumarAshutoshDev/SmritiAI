package com.teamchromium.smritiai.data.local

import android.content.Context
import androidx.room.Room
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also { instance = it }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        SQLiteDatabase.loadLibs(context)

        val secretKey = KeyManager.getOrCreateKey()
        val passphrase = secretKey.encoded
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "smriti_encrypted.db"
        )
            .openHelperFactory(factory)
            .build()
    }
}