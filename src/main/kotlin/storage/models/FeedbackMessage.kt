package org.example.storage.models

data class FeedbackMessage (
    val id: Long,
    val userId: Long,
    val message: String
)