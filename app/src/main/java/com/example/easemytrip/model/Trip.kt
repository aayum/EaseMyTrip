package com.example.easemytrip.model

data class Trip(
    val trip_id: String,
    val start_time: String,
    val end_time: String,
    val locations: ArrayList<Location>
)