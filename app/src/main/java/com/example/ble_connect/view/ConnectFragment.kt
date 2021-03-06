package com.example.ble_connect.view

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ble_connect.R
import com.example.ble_connect.apdater.ScanResultAdapter
import com.example.ble_connect.callback.ItemSelectedCallBack
import com.example.ble_connect.databinding.FragmentConnectBinding


class ConnectFragment : Fragment() {

    private var _binding: FragmentConnectBinding? = null
    private val scanResults = mutableListOf<ScanResult>()
    private val scanFilters = mutableListOf<ScanFilter>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(data = scanResults, callBack = itemSelectedCallBack)
    }
    private val binding get() = _binding!!
    private var isScanning = false
        set(value) {
            field = value
            binding.btnScan.text = if (value) "Stop Scan" else "Start Scan"
        }
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConnectBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val linearLayoutManager = LinearLayoutManager(context)
        val filter = ScanFilter.Builder().setServiceUuid(
            ParcelUuid.fromString(MainActivity.ENVIRONMENTAL_SERVICE_UUID)
        ).build()
        scanFilters.add(filter)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.listDevice.apply {
            layoutManager = linearLayoutManager
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            adapter = scanResultAdapter
            setHasFixedSize(true)
        }
        binding.btnScan.setOnClickListener() {
            if (isScanning) {
                stopBleScan()
            } else {
                startBleScan()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }


    // y??u c???u truy c???p v??? tr??
    private fun requestLocationPermission() {
        if (isLocationPermissionGranted!!) { // cho ph??p r???i th?? th??i
            return
        }
        requestPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            MainActivity.LOCATION_PERMISSION_REQUEST_CODE
        )

    }

    private fun requestPermission(permission: String, requestCode: Int) {
        requestPermissions(arrayOf(permission), requestCode)
    }

    // ???? cho ph??p v??? tr?? ch??a ?
    val isLocationPermissionGranted
        get() = context?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    //ki???m tra xem n???u l?? y??u c???u v??? tr?? th?? check xem c?? ph???i t??? ch???i kh??ng -> y??u c???u l???i
    // ng?????c l???i b???t ?????u scan
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MainActivity.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }

    // tr??? v??? xem context ???? c?? permission ch??a
    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    // ki???m tra xem bluetooth c?? b???t kh??ng, n???u kh??ng th?? hi???n th??? ra 1 c???nh b??o
    // s??? d???ng BluetoothAdapter.ACTION_REQUEST_ENABLE ????? y??u c???u ng?????i d??ng b???t Bluetooth tr??n thi???t b???
    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, MainActivity.ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    // g???i ?????n khi n??o b???t th?? th??i
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MainActivity.ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }


    // b???t ?????u scan
    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted!!) {
            requestLocationPermission() // ???? cho ph??p b???t v??? tr?? ch??a, ch??a th?? y??u c???u
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    // d???ng scan
    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Log.e("ScanCallback", "Found BLE device!Attributes.Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }

    private val itemSelectedCB: ItemSelectedCallBack =
        object : ItemSelectedCallBack {
            override fun callBackConnectDevice(item: ScanResult) {
                if (isScanning) {
                    stopBleScan()
                }

            }

        }
    private val itemSelectedCallBack: ItemSelectedCallBack =
        object : ItemSelectedCallBack {
            override fun callBackConnectDevice(item: ScanResult) {
//                Log.d("TAG", "nCallBack: " + item.device.address)\
                fragmentManager?.beginTransaction()
                    ?.add(
                        R.id.mainActivity, ConnectDeviceFragment.newInstance(
                            itemSelectedCB, item
                        )
                    )?.commitNow()

            }

        }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newConnect() = ConnectFragment()
    }
}