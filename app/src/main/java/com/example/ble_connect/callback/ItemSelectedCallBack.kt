package com.example.ble_connect.callback

import android.bluetooth.le.ScanResult

interface ItemSelectedCallBack {
    fun callBackConnectDevice(item: ScanResult)
}