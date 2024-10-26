package org.example.parser

import org.example.parser.ozon.OzonParserImpl
import org.example.parser.vinylbox.VinylBoxParserImpl
import org.example.parser.yandex.YandexMarketParserImpl

/**
 * Фабрика для создания экземпляров парсеров, соответствующих заданному магазину.
 */
object ParserFactory {

    /**
     * Возвращает экземпляр [Parser] для указанного магазина [ShopName].
     *
     * @param shopName Название магазина, для которого требуется парсер.
     * @return Экземпляр соответствующего реализации [Parser].
     */
    fun make(shopName: ShopName): Parser {
        return when (shopName) {
            ShopName.OZON -> OzonParserImpl()
            ShopName.VINYLBOX -> VinylBoxParserImpl()
            ShopName.YANDEXMARKET -> YandexMarketParserImpl()
        }
    }
}

/**
 * Перечисление, представляющее поддерживаемые магазины.
 *
 * @property OZON Магазин Ozon.
 * @property VINYLBOX Магазин VinylBox.
 * @property YANDEXMARKET Магазин Yandex Market.
 */
enum class ShopName {
    OZON, VINYLBOX, YANDEXMARKET
}

