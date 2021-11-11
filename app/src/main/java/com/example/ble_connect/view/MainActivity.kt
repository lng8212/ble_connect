package com.example.ble_connect.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ble_connect.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainActivity, Connect.newConnect())
                .commitNow()
        }
    }
    companion object {
        const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
        const val LOCATION_PERMISSION_REQUEST_CODE = 2
        const val ENVIRONMENTAL_SERVICE_UUID = "73f91462-410b-11ec-973a-0242ac130003"
    }
}