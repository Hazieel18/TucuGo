package com.tucugo.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tucugo.app.data.models.Vehicle
import com.tucugo.app.databinding.ItemPendingVehicleBinding

class AdminVehiclesAdapter(
    private var vehicles: List<Vehicle>,
    private val onApprove: (Vehicle) -> Unit,
    private val onReject: (Vehicle) -> Unit
) : RecyclerView.Adapter<AdminVehiclesAdapter.VehicleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemPendingVehicleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateData(newList: List<Vehicle>) {
        vehicles = newList
        notifyDataSetChanged()
    }

    inner class VehicleViewHolder(private val binding: ItemPendingVehicleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(vehicle: Vehicle) {
            binding.tvVehicleType.text = vehicle.type
            binding.tvVehicleDetails.text = "${vehicle.brand} ${vehicle.model} - ${vehicle.plateNumber}"
            binding.tvVehicleCapacity.text = "Capacidad: ${vehicle.capacity} pasajeros"
            
            if (vehicle.photoUrls.isNotEmpty()) {
                binding.ivVehiclePhoto.load(vehicle.photoUrls[0]) {
                    crossfade(true)
                }
            }

            binding.btnApproveVehicle.setOnClickListener { onApprove(vehicle) }
            binding.btnRejectVehicle.setOnClickListener { onReject(vehicle) }
        }
    }
}
