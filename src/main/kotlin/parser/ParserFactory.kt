package org.example.parser

import org.example.parser.ozon.OzonParserImpl
import org.example.parser.vinylbox.VinylBoxParserImpl
import org.example.parser.yandex.YandexMarketParserImpl

object ParserFactory {

    fun make(shopName: ShopName): Parser {
        return when (shopName) {
            ShopName.OZON -> OzonParserImpl()
            ShopName.VINYLBOX -> VinylBoxParserImpl()
            ShopName.YANDEXMARKET -> YandexMarketParserImpl()
        }
    }
}

enum class ShopName {
    OZON, VINYLBOX, YANDEXMARKET
}
