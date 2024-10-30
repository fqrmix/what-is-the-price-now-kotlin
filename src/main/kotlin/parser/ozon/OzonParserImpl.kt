package org.example.parser.ozon

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.parser.Parser
import org.example.parser.ParserConstants
import org.example.parser.ShopName
import org.example.parser.selenium.SeleniumManager
import org.example.storage.models.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.net.URL


/**
 * Реализация парсера для получения информации о товарах с сайта Ozon.
 *
 * @property forceUpdate Если `true`, будет обновлять информацию о товаре каждый раз при вызове [getArticleInfo].
 */
class OzonParserImpl(override val forceUpdate: Boolean = true) : Parser {

    companion object {
        val logger = KotlinLogging.logger {}
    }

    /**
     * Объект [Article], содержащий информацию о товаре. Инициализируется при первом запросе информации о товаре
     * или при включенном флаге [forceUpdate].
     */
    private lateinit var article: Article

    private val seleniumManager = SeleniumManager()

    /**
     * Возвращает информацию о товаре с указанного URL.
     * Если информация ранее не была загружена или установлен флаг [forceUpdate], выполняется парсинг страницы.
     *
     * @param articleUrl URL страницы товара.
     * @return [Article] с актуальной информацией о товаре.
     */
    override fun getArticleInfo(articleUrl: URL): Article {
        if (!::article.isInitialized || forceUpdate) {
            parseArticle(articleUrl)
        }
        return article
    }

    /**
     * Выполняет парсинг страницы товара для извлечения информации о названии и цене.
     *
     * @param articleUrl URL страницы товара на Ozon.
     * @throws Exception если не удается получить информацию о товаре с указанной страницы.
     */
    private fun parseArticle(articleUrl: URL) {
        try {

            println("Parsing page...")

            for (i in 1..10) {
                println("Try $i ...")
                seleniumManager.webDriver!!.get(articleUrl.toString())
                sleep(1000)
                if (!seleniumManager.webDriver!!.pageSource?.contains("antibot")!! &&
                    seleniumManager.webDriver!!.pageSource?.isNotEmpty() == true &&
                    !seleniumManager.webDriver!!.title?.lowercase()?.contains("antibot")!!
                ) {
                    sleep(1000)
                    break
                }
            }

            val document: Document = seleniumManager.webDriver!!.pageSource!!.let {
                Jsoup.parse(it)
            }

            val nameString = document.selectXpath(ParserConstants.Ozon.ARTICLE_NAME.value).text()
            val priceString = document.selectXpath(ParserConstants.Ozon.PRICE.value).text()
            val price = BigDecimal(
                priceString.filter { it.isDigit() }
            )
            article = Article(price, nameString, ShopName.OZON, articleUrl.toString())
        } catch (e: Exception) {
            logger.error { e.printStackTrace() }
            throw e
        } finally {
            seleniumManager.webDriver!!.quit()
        }
    }
}
