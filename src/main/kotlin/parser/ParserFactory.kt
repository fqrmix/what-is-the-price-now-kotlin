package org.example.parser

import org.example.parser.drhead.DrHeadParserImpl
import org.example.parser.korobkavinila.KorobkaVinilaParserImpl
import org.example.parser.plastinkacom.PlastinkaComParserImpl
import org.example.parser.pultru.PultRuParserImpl
import org.example.parser.vinylbox.VinylBoxParserImpl

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
//            ShopName.OZON -> OzonParserImpl()
            ShopName.VINYLBOX -> VinylBoxParserImpl()
//            ShopName.YANDEXMARKET -> YandexMarketParserImpl()
            ShopName.PULTRU -> PultRuParserImpl()
            ShopName.DRHEAD -> DrHeadParserImpl()
            ShopName.PLASTINKACOM -> PlastinkaComParserImpl()
            ShopName.KOROBKAVINILA -> KorobkaVinilaParserImpl()
        }
    }
}

/**
 * Перечисление, представляющее поддерживаемые магазины.
 *
 * @property OZON Магазин Ozon.
 * @property VINYLBOX Магазин VinylBox.
 * @property YANDEXMARKET Магазин Yandex Market.
 * @property PULTRU Магазин Pult Ru
 * @property DRHEAD Магазин Dr Head
 * @property PLASTINKACOM Магазин Plastinka Com
 * @property KOROBKAVINILA Магазин "Коробка Винила"
 */
enum class ShopName {
//    OZON,
    VINYLBOX,
//    YANDEXMARKET,
    PULTRU,
    DRHEAD,
    PLASTINKACOM,
    KOROBKAVINILA
}

