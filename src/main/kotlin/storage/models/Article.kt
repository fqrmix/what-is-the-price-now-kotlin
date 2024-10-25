package org.example.storage.models

import org.example.parser.ShopName
import java.math.BigDecimal

data class Article(
    var price: BigDecimal,
    val name: String,
    val shopName: ShopName,
    val url: String
)