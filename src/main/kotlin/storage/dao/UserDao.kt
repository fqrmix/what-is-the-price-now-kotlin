package org.example.storage.dao

import org.example.storage.models.User
import org.example.storage.tables.Users
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class UserDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserDao>(Users)

    var name by Users.name
    var tariff by Users.tariff
    var timeToNotify by Users.timeToNotify

    fun toUser() = User(id.value, name, tariff, timeToNotify)
}

