package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.tucugo.app.R
import com.tucugo.app.data.models.Product
import com.tucugo.app.data.repository.BusinessRepository
import com.tucugo.app.data.repository.CartRepository
import com.tucugo.app.databinding.FragmentBusinessDetailBinding
import com.tucugo.app.ui.adapters.ProductAdapter
import kotlinx.coroutines.launch

class BusinessDetailFragment : Fragment() {

    private var _binding: FragmentBusinessDetailBinding? = null
    private val binding get() = _binding!!
    private val args: BusinessDetailFragmentArgs by navArgs()
    
    private val businessRepository = BusinessRepository()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val businessId = args.businessId
        setupUI()
        loadBusinessData(businessId)
        observeCart()
    }

    private fun setupUI() {
        productAdapter = ProductAdapter(emptyList()) { product ->
            CartRepository.addProduct(product)
            Toast.makeText(context, "${product.name} añadido al carrito", Toast.LENGTH_SHORT).show()
        }
        binding.rvProducts.adapter = productAdapter

        binding.fabCart.setOnClickListener {
            findNavController().navigate(R.id.action_businessDetailFragment_to_cartFragment)
        }
    }

    private fun loadBusinessData(businessId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Cargar información del negocio
            val business = businessRepository.getBusiness(businessId)
            business?.let {
                binding.toolbar.title = it.name
                binding.tvBusinessDescription.text = "Categoría: ${it.category}\nRIF: ${it.rif}"
                // ivBusinessHeader.load(...) si tuviera URL de imagen
            }

            // Cargar productos en tiempo real
            businessRepository.getBusinessProducts(businessId).addSnapshotListener { snapshot, _ ->
                val products = snapshot?.toObjects(Product::class.java) ?: emptyList()
                productAdapter.updateData(products)
            }
        }
    }

    private fun observeCart() {
        CartRepository.cartItems.observe(viewLifecycleOwner) {
            val totalItems = it.values.sumOf { list -> list.sumOf { item -> item.quantity } }
            binding.fabCart.text = "Ver Carrito ($totalItems)"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
