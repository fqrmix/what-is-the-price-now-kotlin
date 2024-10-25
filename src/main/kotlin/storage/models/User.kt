package org.example.storage.models

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val tariff: Tariff
)