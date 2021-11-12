package com.example.ble_connect.view

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ble_connect.R
import com.example.ble_connect.callback.ItemSelectedCallBack
import com.example.ble_connect.databinding.DetailFragmentBinding
import com.example.ble_connect.databinding.FragmentConnectBinding
import com.example.ble_connect.model.Service

class ConnectDeviceFragment (
    private val mCallBack: ItemSelectedCallBack,
    private val item: ScanResult) : Fragment() {
    private var _binding: DetailFragmentBinding? = null
    private val binding get() = _binding!!
    private var connected = false
        set(value) {
            field = value
            runOnUiThread {
                binding.btnConnect.text =
                    if (value) "Ngắt kết nối với thiết bị" else "Kết nối với thiết bị"
            }
        }
    private lateinit var mContext: Context
    private lateinit var listService: MutableList<Service>
    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        listenToBondStateChanges(mContext)

    }
    private fun initView(){
        binding.txtNamedevice.text = item.device.name ?:"unnamed"
        ("Address: " + item.device.address.toString()).also { binding.txtAddressdetail.text = it }
        binding.btnClose.setOnClickListener(){
            fragmentManager?.beginTransaction()
                ?.remove(this)
                ?.commitNow()
        }
        binding.btnConnect.setOnClickListener(){
            mCallBack.callBackConnectDevice(item)
            with(item.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
                runOnUiThread {
                    binding.btnConnect.text = "Đang kết nối tới $address"
                }
                if (connected) {
                } else {
                    connectGatt(context, false, gattCallback)

                }
            }
        }
    }
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    connected = true
                    Handler(Looper.getMainLooper()).post {
                        gatt.discoverServices()
                    }
                    gatt.device.createBond()
                    // TODO: Store a reference to BluetoothGatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    connected= false
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                connected = false
                gatt.close()
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w(
                    "BluetoothGattCallback",
                    "Discovered ${services.size} services for ${device.address}"
                )
                getGattServiceDetails() // See implementation just above this section
                // Consider connection setup as complete here
            }
        }
    }
    private fun BluetoothGatt.getGattServiceDetails() {
        val listService = StringBuilder()
        if (services.isEmpty()) {
            Log.i(
                "printGattTable",
                "No service and characteristic available, call discoverServices() first?"
            )
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n",
            ) { it.uuid.toString() }
            listService.append("Service\n ${service.uuid}\nCharacteristics:\n$characteristicsTable\n\n")
            Log.i(
                "printGattTable",
                "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
        runOnUiThread {
            binding.txtAllService.text = listService
        }

    }

    private fun listenToBondStateChanges(context: Context) {
        context.applicationContext.registerReceiver(
            broadcastReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )
    }
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            with(intent) {
                if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val device = getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val previousBondState =
                        getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
                    val bondState = getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val bondTransition = "${previousBondState.toBondStateDescription()} to " +
                            bondState.toBondStateDescription()
                    Log.w(
                        "Bond state change",
                        "${device?.address} bond state changed | $bondTransition"
                    )
                }
            }
        }

        private fun Int.toBondStateDescription() = when (this) {
            BluetoothDevice.BOND_BONDED -> "BONDED"
            BluetoothDevice.BOND_BONDING -> "BONDING"
            BluetoothDevice.BOND_NONE -> "NOT BONDED"
            else -> "ERROR: $this"
        }
    }

    companion object {
        fun newInstance(
            mCallBack: ItemSelectedCallBack,
            item: ScanResult
        ) = ConnectDeviceFragment(mCallBack, item)
    }
}