package org.example.parser

import org.example.storage.models.Article

interface Parser {

    val forceUpdate: Boolean

    fun getArticleInfo(articleUrl: String): Article
}