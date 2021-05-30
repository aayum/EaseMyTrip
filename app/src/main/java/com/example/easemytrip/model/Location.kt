package com.example.easemytrip.model

data class Location(
    val accuracy: Double = 10.0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)
