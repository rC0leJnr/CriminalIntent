package com.rick.criminalintent.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.rick.criminalintent.database.CrimeDatabase
import com.rick.criminalintent.model.Crime
import com.rick.criminalintent.util.Constants.DATABASE_NAME
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

class CrimeRepository private constructor(context: Context){

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val crimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime) {
        executor.execute{
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute{
            crimeDao.addCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)

    companion object{
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get() = INSTANCE ?: throw IllegalStateException("CrimeRepository not initiazed")
    }

}