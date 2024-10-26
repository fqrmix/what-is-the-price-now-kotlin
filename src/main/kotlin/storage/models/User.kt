package org.example.storage.models

import java.time.LocalTime

data class User(
    val id: Long,
    val name: String,
    val tariff: Tariff,
    var timeToNotify: LocalTime? = null
)