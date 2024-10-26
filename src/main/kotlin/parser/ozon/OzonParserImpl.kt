package org.example.parser.ozon

import org.example.storage.models.Article
import org.example.parser.Parser
import org.example.parser.ShopName
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.net.URL


class OzonParserImpl(override val forceUpdate: Boolean = true) : Parser {

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
            val priceString = document.selectXpath("//*[@id=\"layoutPage\"]/div[1]/div[4]/div[3]/div[2]/div/div/div[1]/div[2]/div/div[1]/div/div/div[1]/div[2]/div/div[1]/span[1]")?.text()
            val nameString = document.selectXpath("//*[@id=\"layoutPage\"]/div[1]/div[4]/div[3]/div[1]/div[1]/div[2]/div/div/div/div[1]/h1").text()
            if (priceString != null) {
                val price = BigDecimal(
                    priceString.trim()
                        .replace(" ", "")
                        .replace("руб.", "")
                )
                article = Article(price, nameString, ShopName.OZON, articleUrl.toString())
            }
        } catch (e: Exception) {
            println(e)
            throw e
        }
    }
}