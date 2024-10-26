package org.example.storage.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Articles : LongIdTable() {
    val price = decimal("price", 10, 2)
    val name = varchar("name", 255)
    val shopName = varchar("shop_name", 255)
    val url = varchar("url", 2048)
}