package org.example.storage.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object FeedbackMessages: LongIdTable() {
    val userId = reference("user_id", Users)
    val message = varchar(name = "message", length = 1024)
}