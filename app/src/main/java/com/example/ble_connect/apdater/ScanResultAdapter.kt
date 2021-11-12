package com.example.ble_connect.apdater

import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ble_connect.callback.ItemSelectedCallBack
import com.example.ble_connect.databinding.ItemDeviceBinding

class ScanResultAdapter(private  val data : MutableList<ScanResult> = mutableListOf(),private val callBack: ItemSelectedCallBack):RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {
    private lateinit var context: Context
    private lateinit var binding: ItemDeviceBinding
    inner class ViewHolder(binding: ItemDeviceBinding ) : RecyclerView.ViewHolder(binding.root) {
        private val title = binding.titleDevice
        private val mAddress = binding.txtAddress
        private val RSSI = binding.txtRSSI
        fun bind (scanResult: ScanResult){
            title.text = scanResult.device.name?: "unnamed"
            mAddress.text = scanResult.device.address.toString()
            (scanResult.rssi.toString() + "dBm").also { RSSI.text = it }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        context = parent.context
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener(){
            callBack.callBackConnectDevice(data[position])
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }
}
