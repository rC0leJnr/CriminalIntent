package com.rick.criminalintent.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.rick.criminalintent.util.Constants.ARG_DATE
import java.util.*

class DataPickerFragment: DialogFragment() {

    interface  Callbacks {
        fun onDateSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val resultDate = GregorianCalendar(year, month, day).time

            targetFragment?.let {
                (it as Callbacks).onDateSelected(resultDate)
            }
        }
        val data = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = data
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDate = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDate
        )

    }

    companion object {
        fun newInstance(date: Date): DataPickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return  DataPickerFragment().apply{
                arguments = args
            }
        }
    }

}