package org.example.storage.tables

import org.example.storage.models.Tariff
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.javatime.time

object Users : IdTable<Long>("Users") {
    override val id = long("id").entityId().uniqueIndex()
    override val primaryKey = PrimaryKey(id)
    val name = varchar("name", 255)
    val tariff = enumerationByName("tariff", 50, Tariff::class)
    var timeToNotify = time("time_to_notify").nullable()
}