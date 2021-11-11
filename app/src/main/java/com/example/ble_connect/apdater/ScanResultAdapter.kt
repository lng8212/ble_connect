package com.example.ble_connect.apdater

import android.bluetooth.le.ScanResult
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ble_connect.databinding.ItemDeviceBinding

class ScanResultAdapter(private  val data : MutableList<ScanResult> = mutableListOf()):RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {
    lateinit var context: Context
    lateinit var binding: ItemDeviceBinding
    inner class ViewHolder(binding: ItemDeviceBinding ) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.titleDevice
        val UUID = binding.txtUUID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        context = parent.context
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = data[position].device.name?: "unnamed"
        holder.UUID.text = data[position].device.address.toString()
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
