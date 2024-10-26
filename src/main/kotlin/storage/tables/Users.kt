package org.example.storage.tables

import org.example.storage.models.Tariff
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.javatime.JavaLocalTimeColumnType
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.time
import java.time.LocalTime

object Users : IdTable<Long>("Users") {
    override val id = long("id").entityId().uniqueIndex()
    override val primaryKey = PrimaryKey(id)
    val name = varchar("name", 255)
    val tariff = enumerationByName("tariff", 50, Tariff::class)
    var timeToNotify = time("time_to_notify").nullable()
//    var timeToNotify = varchar("time_to_notify", 7).nullable()
}