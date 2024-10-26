package org.example.parser

import org.example.storage.models.Article
import java.net.URL

interface Parser {

    val forceUpdate: Boolean

    fun getArticleInfo(articleUrl: URL): Article
}