package org.example.parser.vinylbox

import org.example.storage.models.Article
import org.example.parser.Parser
import org.example.parser.ShopName
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.net.URL


class VinylBoxParserImpl(override val forceUpdate: Boolean = true) : Parser {

    private lateinit var article: Article
    override fun getArticleInfo(articleUrl: URL): Article {
        if (!::article.isInitialized || forceUpdate) {
            parseArticle(articleUrl)
        }
        return article
    }

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