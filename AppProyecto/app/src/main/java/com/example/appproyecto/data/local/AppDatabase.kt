package com.example.appproyecto.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.appproyecto.data.model.Evidencia

/** Base local: solo guarda las evidencias fotográficas. Todo lo demás vive en el backend. */
@Database(
    entities = [Evidencia::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun evidenciaDao(): EvidenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder<AppDatabase>(
                    context.applicationContext,
                    "oportunidades.db"
                )
                    .setDriver(BundledSQLiteDriver())
                    // Al subir la versión se descartan las tablas antiguas (usuarios, oportunidades...)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}