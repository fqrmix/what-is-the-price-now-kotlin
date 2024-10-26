package org.example.parser.yandex

import org.example.parser.Parser
import org.example.parser.ShopName
import org.example.storage.models.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.net.URL

class YandexMarketParserImpl(override val forceUpdate: Boolean = true) : Parser {

    private lateinit var article: Article

    override fun getArticleInfo(articleUrl: URL): Article {
        if (!::article.isInitialized || forceUpdate) {
            parseArticle(articleUrl)
        }
        return article
    }

    private fun parseArticle(articleUrl: URL) {
        try {
            println("Parsing page...")
            val document: Document = Jsoup.connect(articleUrl.toString()).get()
            val priceString = document.selectXpath("//*[@id=\"/content/page/fancyPage/defaultPage/mainDO\"]/div/div[2]/div[1]/div[4]/div[1]/div/div[2]/div[1]/div/div/div[2]/div[1]/div/div[1]/h3")?.text()
            val nameString = document.selectXpath("//*[@id=\"/content/page/fancyPage/defaultPage/productTitle\"]/h1").text()
            if (priceString != null) {
                val price = BigDecimal(
                    priceString.filter { it.isDigit() }
                )
                article = Article(price, nameString, ShopName.YANDEXMARKET, articleUrl.toString())
            }
        } catch (e: Exception) {
            println(e.printStackTrace())
            throw e
        }
    }
}