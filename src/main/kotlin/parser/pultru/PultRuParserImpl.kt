package org.example.parser.pultru

import org.example.parser.Parser
import org.example.parser.ParserConstants
import org.example.parser.ShopName
import org.example.storage.models.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.net.URL


/**
 * Реализация парсера для получения информации о товарах с сайта Pult Ru.
 *
 * @property forceUpdate Если `true`, каждый вызов метода [getArticleInfo] будет повторно загружать данные о товаре.
 */
class PultRuParserImpl(override val forceUpdate: Boolean = true) : Parser {

    /**
     * Объект [Article], содержащий информацию о товаре. Инициализируется при первом запросе
     * или при включенном флаге [forceUpdate].
     */
    private lateinit var article: Article

    /**
     * Возвращает информацию о товаре по заданному URL.
     * Если информация ранее не была загружена или установлен флаг [forceUpdate], выполняется парсинг страницы.
     *
     * @param articleUrl URL страницы товара.
     * @return Объект [Article], содержащий актуальную информацию о товаре.
     */
    override fun getArticleInfo(articleUrl: URL): Article {
        if(!::article.isInitialized || forceUpdate) {
            parseArticle(articleUrl)
        }
        return article
    }

    /**
     * Выполняет парсинг страницы Pult Ru для извлечения информации о названии и цене товара.
     *
     * @param articleUrl URL страницы товара на Pult Ru.
     * @throws Exception если возникла ошибка при попытке извлечь данные.
     */
    private fun parseArticle(articleUrl: URL) {
        try {
            println("Parsing page...")
            val document: Document = Jsoup.connect(articleUrl.toString()).get()
            val priceString = document.select(ParserConstants.PultRu.PRICE.value).text()
            val nameString = document.select(ParserConstants.PultRu.ARTICLE_NAME.value).text()
            val price = BigDecimal(
                priceString.filter { it.isDigit() }
            )
            article = Article(price, nameString, ShopName.PULTRU, articleUrl.toString())
        } catch (e: Exception) {
            println(e.printStackTrace())
            throw e
        }
    }
}