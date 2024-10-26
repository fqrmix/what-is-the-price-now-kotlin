package org.example.storage.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Subscriptions : LongIdTable() {
    val userId = reference("user_id", Users)
    val articleId = reference("article_id", Articles)
    val createdTime = datetime("created_time")
    val nextExecutionTime = datetime("next_execution_time")
}