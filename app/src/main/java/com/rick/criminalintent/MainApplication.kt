package com.rick.criminalintent

import android.app.Application
import com.rick.criminalintent.viewmodel.CrimeRepository

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}