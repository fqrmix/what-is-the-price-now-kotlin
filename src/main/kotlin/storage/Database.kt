package org.example.storage

import org.example.storage.tables.Articles
import org.example.storage.tables.Subscriptions
import org.example.storage.tables.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Database {

    fun connectToDatabase() {
        Database.connect(
            url = "jdbc:postgresql://postgres:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "mysecretpassword"
        )

        transaction {
            SchemaUtils.create(Users, Articles, Subscriptions)
        }
    }
}