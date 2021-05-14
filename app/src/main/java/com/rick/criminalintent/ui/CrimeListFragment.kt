package com.rick.criminalintent.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rick.criminalintent.R
import com.rick.criminalintent.databinding.FragmentCrimeListBinding
import com.rick.criminalintent.databinding.ListItemCrimeBinding
import com.rick.criminalintent.model.Crime
import com.rick.criminalintent.viewmodel.CrimeListViewModel
import java.util.*

class CrimeListFragment: Fragment() {

    private var _binding: FragmentCrimeListBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemBinding: ListItemCrimeBinding
    private var crimeAdapter: CrimeAdapter? = CrimeAdapter(emptyList())
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }
    private var callbacks: Callbacks? = null

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as? Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.crimeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = crimeAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
                    updateUI(it)
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        callbacks =  null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?. onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(crimes: List<Crime>) {
        crimeAdapter?.let{
            it.crimes = crimes
        } ?: run {
            crimeAdapter = CrimeAdapter(crimes)
        }
        binding.crimeRecyclerView.adapter = crimeAdapter
    }

    private inner class  CrimeHolder(binding: ListItemCrimeBinding)
        :RecyclerView.ViewHolder(binding.root), View.OnClickListener{

        private lateinit var crime: Crime

        init {
            itemView.setOnClickListener { this }
        }

        fun bind(crime: Crime){
            this.crime = crime
            itemBinding.apply{
                crimeTitle.text = crime.title
                crimeDate.text = crime.date.toString()
                crimeSolved.visibility = if(crime.isSolved) {
                    View.VISIBLE
                } else {View.GONE}
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>)
        : RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            itemBinding = ListItemCrimeBinding.inflate(LayoutInflater.from(parent.context))
            return CrimeHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemCount() = crimes.size
    }

    companion object {
        fun newInstance() : CrimeListFragment {
            return newInstance()
        }
    }
}































