package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tucugo.app.data.repository.BusinessRepository
import com.tucugo.app.data.repository.CartRepository
import com.tucugo.app.databinding.FragmentSearchBinding
import com.tucugo.app.ui.adapters.ProductAdapter
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    
    private val businessRepository = BusinessRepository()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearchInput()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList()) { product ->
            CartRepository.addProduct(product)
            Toast.makeText(context, "${product.name} añadido al carrito", Toast.LENGTH_SHORT).show()
        }
        binding.rvSearchResults.adapter = productAdapter
    }

    private fun setupSearchInput() {
        binding.etSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.etSearchQuery.text.toString().trim())
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        binding.progressBar.visibility = View.VISIBLE
        binding.tvNoResults.visibility = View.GONE
        productAdapter.updateData(emptyList())

        viewLifecycleOwner.lifecycleScope.launch {
            val results = businessRepository.searchProducts(query)
            binding.progressBar.visibility = View.GONE
            
            if (results.isEmpty()) {
                binding.tvNoResults.visibility = View.VISIBLE
            } else {
                productAdapter.updateData(results)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
