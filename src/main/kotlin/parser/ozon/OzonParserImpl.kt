package org.example.parser.ozon

import org.example.storage.models.Article
import org.example.parser.Parser


class OzonParserImpl(override val forceUpdate: Boolean = true) : Parser {

    override fun getArticleInfo(articleUrl: String): Article {
        TODO("Not yet implemented")
    }
}