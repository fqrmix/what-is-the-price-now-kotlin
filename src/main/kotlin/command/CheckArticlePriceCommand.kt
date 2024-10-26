package org.example.command

import org.example.storage.models.Article
import java.math.BigDecimal
import java.net.URL


/**
 * Команда для проверки актуальной цены товара.
 */
class CheckArticlePriceCommand {

    /**
     * Выполняет команду для получения актуальной цены товара.
     *
     * @param article Объект [Article], содержащий информацию о товаре.
     * @return [BigDecimal] с актуальной ценой товара.
     * @throws RuntimeException если не удаётся получить актуальную информацию о товаре.
     */
    fun execute(article: Article): BigDecimal {
        val actualArticleInfo = getArticleInfo(URL(article.url))
        if (actualArticleInfo != null) {
            return actualArticleInfo.price
        } else {
            throw RuntimeException("Cannot get actual article price")
        }
    }

    /**
     * Получает актуальную информацию о товаре по указанному URL.
     *
     * @param url [URL] страницы товара.
     * @return Объект [Article] с актуальной информацией о товаре или `null`, если информация недоступна.
     */
    private fun getArticleInfo(url: URL): Article? {
        return ParseArticleCommand().execute(url)
    }
}

