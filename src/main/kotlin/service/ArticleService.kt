package org.example.service

import org.example.command.ParseArticleCommand
import org.example.storage.models.Article
import java.net.URL

class ArticleService {

    fun parseArticle(url: URL): Article? {
        return ParseArticleCommand().execute(url)
    }
}