package com.rick.criminalintent.viewmodel

import androidx.lifecycle.ViewModel
import com.rick.criminalintent.model.Crime

class CrimeListViewModel: ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime){
        crimeRepository.addCrime(crime)
    }
}