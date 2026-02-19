package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tucugo.app.R
import com.tucugo.app.databinding.FragmentDashboardBinding
import com.tucugo.app.ui.adapters.BusinessAdapter
import com.tucugo.app.ui.adapters.CategoryAdapter
import com.tucugo.app.ui.viewmodel.DashboardViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBusinesses.layoutManager = LinearLayoutManager(context)

        binding.cardRide.setOnClickListener {
            findNavController().navigate(R.id.rideRequestFragment)
        }

        // Búsqueda rápida
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.nav_orders) {
                findNavController().navigate(R.id.nav_orders)
                true
            } else false
        }
    }

    private fun setupObservers() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.rvCategories.adapter = CategoryAdapter(categories) { category ->
                // Filtrar lógica
            }
        }

        val businessAdapter = BusinessAdapter(emptyList()) { business ->
            val action = DashboardFragmentDirections.actionNavHomeToBusinessDetailFragment(business.id)
            findNavController().navigate(action)
        }
        binding.rvBusinesses.adapter = businessAdapter

        viewModel.businesses.observe(viewLifecycleOwner) {
            businessAdapter.updateData(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
