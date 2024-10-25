package org.example.parser.yandex

import org.example.parser.Parser
import org.example.storage.models.Article

class YandexMarketParserImpl(override val forceUpdate: Boolean = true) : Parser {
    override fun getArticleInfo(articleUrl: String): Article {
        TODO("Not yet implemented")
    }
}