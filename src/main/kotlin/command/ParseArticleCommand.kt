package org.example.command

import org.example.common.context.Context
import org.example.common.context.SubscriptionContext
import org.example.parser.ParserFactory
import org.example.parser.Parser
import org.example.parser.ShopName
import org.example.storage.models.Article
import java.net.URL

class ParseArticleCommand {

    private lateinit var parser: Parser

    fun execute(url: URL): Article? {
        val shopName = getShopNameByUrl(url)
        if (shopName != null) {
            parser = ParserFactory.make(shopName)
            return parser.getArticleInfo(url)
        } else return null
    }

    private fun getShopNameByUrl(url: URL): ShopName? {
        return when(url.host) {
            "www.vinylbox.ru", "vinylbox.ru" -> ShopName.VINYLBOX
            "www.ozon.ru", "ozon.ru" -> ShopName.OZON
            "market.yandex.ru", "www.market.yandex.ru" -> ShopName.YANDEXMARKET
            else -> {
                return null
            }
        }
    }
}