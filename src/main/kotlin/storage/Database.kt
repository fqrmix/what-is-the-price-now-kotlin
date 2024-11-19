package org.example.storage

import org.example.storage.tables.Articles
import org.example.storage.tables.FeedbackMessages
import org.example.storage.tables.Subscriptions
import org.example.storage.tables.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Database {

    fun connectToDatabase() {
        Database.connect(
            url = System.getenv("POSTGRES_URL"),
            driver = "org.postgresql.Driver",
            user = System.getenv("POSTGRES_USER"),
            password = System.getenv("POSTGRES_PASSWORD"),
        )

        transaction {
            SchemaUtils.create(Users, Articles, Subscriptions, FeedbackMessages)
        }
    }
}