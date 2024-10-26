package org.example.parser.vinylbox

import org.example.storage.models.Article
import org.example.parser.Parser
import org.example.parser.ShopName
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.net.URL


/**
 * Реализация парсера для получения информации о товарах с сайта VinylBox.
 *
 * @property forceUpdate Если `true`, каждый вызов метода [getArticleInfo] будет повторно загружать данные о товаре.
 */
class VinylBoxParserImpl(override val forceUpdate: Boolean = true) : Parser {

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
        if (!::article.isInitialized || forceUpdate) {
            parseArticle(articleUrl)
        }
        return article
    }

    /**
     * Выполняет парсинг страницы товара на VinylBox для извлечения информации о названии и цене.
     *
     * @param articleUrl URL страницы товара на VinylBox.
     * @throws Exception если не удается получить информацию о товаре с указанной страницы.
     */
    private fun parseArticle(articleUrl: URL) {
        println("Parsing page...")
        val document: Document = Jsoup.connect(articleUrl.toString()).get()
        val priceString = document.selectFirst("span#block_price")?.text()
        val nameString =
            document.selectXpath("//*[@id=\"middle-column\"]/div/div[2]/form/table/tbody/tr/td[2]/div/div[1]/span[2]/a")
                .text() + " - " +
                    document.selectXpath("//*[@id=\"middle-column\"]/div/div[2]/form/table/tbody/tr/td[2]/div/div[2]/span[2]/a")
                        .text()
        if (priceString != null) {
            val price = BigDecimal(
                priceString.trim()
                    .replace(" ", "")
                    .replace("руб.", "")
            )
            article = Article(price, nameString, ShopName.VINYLBOX, articleUrl.toString())
        }
    }
}