package org.example.command

import org.example.parser.ParserFactory
import org.example.parser.Parser
import org.example.parser.ShopName
import org.example.storage.models.Article
import java.net.URL

/**
 * Команда для анализа и получения информации о товаре по URL.
 */
class ParseArticleCommand {

    /**
     * Парсер для обработки информации о товаре. Инициализируется при выполнении команды.
     */
    private lateinit var parser: Parser

    /**
     * Выполняет команду для получения информации о товаре по URL.
     *
     * @param url [URL] страницы товара.
     * @return Объект [Article] с информацией о товаре, если удаётся определить магазин и получить данные, иначе `null`.
     */
    fun execute(url: URL): Article? {
        val shopName = getShopNameByUrl(url) ?: return null
        parser = ParserFactory.make(shopName)
        return parser.getArticleInfo(url)
    }

    /**
     * Определяет название магазина по URL.
     *
     * @param url [URL] страницы товара.
     * @return [ShopName] соответствующего магазина, если URL совпадает с известными доменами, иначе `null`.
     */
    private fun getShopNameByUrl(url: URL): ShopName? {
        return when(url.host) {
            "www.vinylbox.ru", "vinylbox.ru" -> ShopName.VINYLBOX
//            "www.ozon.ru", "ozon.ru" -> ShopName.OZON
//            "market.yandex.ru", "www.market.yandex.ru" -> ShopName.YANDEXMARKET
            "plastinka.com", "www.platinka.com" -> ShopName.PLASTINKACOM
            "doctorhead.ru", "www.doctorhead.ru" -> ShopName.DRHEAD
            "pult.ru", "www.pult.ru" -> ShopName.PULTRU
            "korobkavinyla.ru", "www.korobkavinyla.ru" -> ShopName.KOROBKAVINILA
            else -> {
                return null
            }
        }
    }
}
