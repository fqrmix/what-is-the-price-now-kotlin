package org.example.parser.yandex

import org.example.parser.Parser
import org.example.parser.ParserConstants
import org.example.storage.models.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.net.URL

/**
 * Реализация парсера для получения информации о товарах с сайта Yandex Market.
 *
 * @property forceUpdate Если `true`, при каждом вызове [getArticleInfo] будет производиться повторное обновление данных о товаре.
 */
class YandexMarketParserImpl(override val forceUpdate: Boolean = true) : Parser {

    /**
     * Объект [Article], содержащий информацию о товаре, загружаемый при первом вызове
     * или при установленном флаге [forceUpdate].
     */
    private lateinit var article: Article

    /**
     * Возвращает актуальную информацию о товаре по заданному URL.
     * Если информация еще не была загружена или [forceUpdate] включен, вызывает метод для парсинга страницы.
     *
     * @param articleUrl URL страницы товара на Yandex Market.
     * @return Объект [Article], содержащий актуальную информацию о товаре.
     */
    override fun getArticleInfo(articleUrl: URL): Article {
        if (!::article.isInitialized || forceUpdate) {
            parseArticle(articleUrl)
        }
        return article
    }

    /**
     * Выполняет парсинг страницы Yandex Market для извлечения информации о названии и цене товара.
     *
     * @param articleUrl URL страницы товара на Yandex Market.
     * @throws Exception если возникла ошибка при попытке извлечь данные.
     */
    private fun parseArticle(articleUrl: URL) {
        try {
            println("Parsing page...")
            val document: Document = Jsoup.connect(articleUrl.toString()).get()
            val priceString = document.selectXpath(ParserConstants.YandexMarket.PRICE.value).text()
            val nameString = document.selectXpath(ParserConstants.YandexMarket.ARTICLE_NAME.value).text()
            val price = BigDecimal(
                priceString.filter { it.isDigit() }
            )
//            article = Article(price, nameString, ShopName.YANDEXMARKET, articleUrl.toString())
        } catch (e: Exception) {
            println(e.printStackTrace())
            throw e
        }
    }
}