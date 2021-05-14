package com.rick.criminalintent.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.rick.criminalintent.R
import com.rick.criminalintent.databinding.ActivityMainBinding
import com.rick.criminalintent.databinding.FragmentCrimeBinding
import com.rick.criminalintent.model.Crime
import com.rick.criminalintent.util.Constants.ARG_CRIME_ID
import com.rick.criminalintent.util.Constants.DATE_FORMAT
import com.rick.criminalintent.util.Constants.DIALOG_DATE
import com.rick.criminalintent.util.Constants.REQUEST_CONTACT
import com.rick.criminalintent.util.Constants.REQUEST_DATE
import com.rick.criminalintent.util.Constants.REQUEST_PHOTO
import com.rick.criminalintent.util.getScaledBitmap
import com.rick.criminalintent.viewmodel.CrimeDetailViewModel
import java.io.File
import java.util.*

class CrimeFragment: Fragment(), DataPickerFragment.Callbacks {

    private var _binding: FragmentCrimeBinding? = null
    private val binding get() = _binding!!

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private val crimeDeetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDeetailViewModel.loadCrime(crimeId)
        crimeDeetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer {crime ->
                crime?.let{
                    this.crime = crime
                    photoFile = crimeDeetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.rick.criminalintent.fileProvider",
                        photoFile)
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                return
            }
        }

        binding.apply {
            crimeTitle.addTextChangedListener(titleWatcher)

            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }

            crimeDate.setOnClickListener {
                DataPickerFragment.newInstance(crime.date).apply {
                    setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                    show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
                }
            }

            crimeReport.setOnClickListener {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject)
                    )
                }.also {intent ->
                    val chooserIntent =
                        Intent.createChooser(intent, getString(R.string.send_report))
                    startActivity(chooserIntent)
                }
            }

            crimeSuspect.apply{
                val pickContactIntent =
                    Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

                setOnClickListener {
                    startActivityForResult(pickContactIntent, REQUEST_CONTACT)
                }

                val packageManager: PackageManager = requireActivity().packageManager
                val resolveActivity: ResolveInfo? =
                    packageManager.resolveActivity(pickContactIntent,
                        PackageManager.MATCH_DEFAULT_ONLY)
                if (resolveActivity == null) {
                    isEnabled = false
                }
            }

            crimePhoto.apply {
                val packageManager = requireActivity().packageManager

                val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val resolvedActivity: ResolveInfo?   =
                    packageManager.resolveActivity(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY)
                if (resolvedActivity == null) {
                    isEnabled = false
                }

                setOnClickListener {
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                    val cameraActivities: List<ResolveInfo> =
                        packageManager.queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY)

                    for (cameraActivity in cameraActivities) {
                        requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }

                    startActivityForResult(captureImage, REQUEST_PHOTO)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDeetailViewModel.saveCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        // Revoke photo permissions if the user4 leaves without taking a photo
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    fun updateUI(){
        binding.apply {
            crimeTitle.setText(crime.title)
            crimeDate.text = crime.date.toString()
            crimeSolved.isChecked = crime.isSolved
            if (crime.suspect.isNotEmpty()){
                crimeSuspect.text = crime.suspect
            }
            updatePhotoView()
        }
    }

    private fun updatePhotoView(){
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            binding.crimePhoto.apply {
                setImageBitmap(bitmap)
                contentDescription =
                    getString(R.string.crime_photo_image_description)
            }
        } else {
            binding.crimePhoto.apply {
                setImageDrawable(null)
                contentDescription =
                    getString(R.string.crime_photo_no_image_description)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                // Specify which fields you want your query to return values for.
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                // Perform your query - the contactUri is like a "where" clause here
                val cursor = requireActivity().contentResolver
                    .query(contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    // Verify cursor contains at least one result
                    if(it.count == 0) {
                        return
                    }

                    // Pull out the first column o fthe first row of data -
                    // that is your suspect's name
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDeetailViewModel.saveCrime(crime)
                    binding.crimeSuspect.text = suspect
                }
            }

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                updatePhotoView()
            }

            /*
             * am about to get in the mooooooddd
             *
             * two goats
             * three goats
             *
             * Bow
             *
             * meu pe ja esta no pescoso
             *
             * tipo aqui a ideia nao escrever a letra da musica ao ritmo que ele repper? rapper
             *
             * nem sei o que vou escrever, mas a ideia e so ver como meus dedos ficam quando eu estou
             * a teclar, digitar
             * deixa ev
             *  */
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if(crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {getString(R.string.crime_report_unsolved)}

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return  getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    companion object {
         fun newInstance(crimeId: UUID): CrimeFragment {
             val args = Bundle().apply {
                 putSerializable(
                         ARG_CRIME_ID, crimeId
                 )
             }
             return CrimeFragment().apply{arguments = args}
         }
    }

}




















