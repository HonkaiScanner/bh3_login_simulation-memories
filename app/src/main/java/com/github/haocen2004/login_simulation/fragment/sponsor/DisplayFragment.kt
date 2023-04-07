package com.github.haocen2004.login_simulation.fragment.sponsor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.haocen2004.login_simulation.adapter.LoadStateAdapter
import com.github.haocen2004.login_simulation.adapter.SponsorAdapter
import com.github.haocen2004.login_simulation.data.sponsor.SponsorViewModel
import com.github.haocen2004.login_simulation.data.sponsor.SponsorViewModelFactory
import com.github.haocen2004.login_simulation.data.sponsor.database.SponsorDao
import com.github.haocen2004.login_simulation.data.sponsor.database.SponsorDatabase
import com.github.haocen2004.login_simulation.databinding.FragmentSpDisplayBinding
import com.github.haocen2004.login_simulation.utils.Logger
import kotlinx.coroutines.launch

class DisplayFragment : Fragment() {
    private lateinit var binding: FragmentSpDisplayBinding
    private lateinit var dao: SponsorDao
    private val viewModel: SponsorViewModel by viewModels { SponsorViewModelFactory(dao) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerViewSp = binding.recyclerViewSp
        val sponsorAdapter = SponsorAdapter(activity)
        recyclerViewSp.layoutManager = LinearLayoutManager(context)
        recyclerViewSp.adapter = sponsorAdapter

        dao = SponsorDatabase.getInstance(requireContext()).sponsorDao()


        recyclerViewSp.adapter = sponsorAdapter.withLoadStateFooter(
            LoadStateAdapter()
        )

        lifecycleScope.launch {
            viewModel.data.collect {
                Logger.d("TAG", "loading data $it")
                sponsorAdapter.submitData(it)
            }
        }

    }

}