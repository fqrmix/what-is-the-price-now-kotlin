package org.example.storage.models

import java.time.LocalDateTime

data class Subscription(
    val id: Long,
    val userId: Long,
    var article: Article,
    val createdTime: LocalDateTime,
    var nextExecutionTime: LocalDateTime
)