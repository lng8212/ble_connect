package com.example.ble_connect.model

data class Service (private val service: String,
                    private val listOfCharacteristics: MutableList<String>?){
}