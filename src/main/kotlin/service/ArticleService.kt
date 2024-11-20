package org.example.service

import org.example.command.CheckArticlePriceCommand
import org.example.command.ParseArticleCommand
import org.example.storage.models.Article
import java.math.BigDecimal
import java.net.URL
import kotlinx.coroutines.*

/**
 * Сервис для работы с товарами, обеспечивающий функции парсинга и проверки изменений цены.
 */
class ArticleService {

    /**
     * Выполняет парсинг страницы по указанному URL и возвращает информацию о товаре.
     *
     * @param url URL страницы товара.
     * @return Объект [Article], содержащий данные о товаре, или `null`, если парсинг не удался.
     */
    suspend fun parseArticle(url: URL): Article? {
        return withContext(Dispatchers.IO) {
            ParseArticleCommand().execute(url)
        }
    }

    /**
     * Проверяет, изменилась ли цена товара по сравнению с текущей.
     *
     * @param article Объект [Article] с текущими данными о товаре.
     * @return Пара [Pair], где первый элемент - новая цена (если изменилась), а второй элемент - `true`,
     * если цена изменилась, или `false`, если осталась прежней.
     */
    suspend fun checkPriceChange(article: Article): Pair<BigDecimal?, Boolean> {
        return withContext(Dispatchers.IO) {
            val newPrice = CheckArticlePriceCommand().execute(article)
             if (article.price != newPrice) {
                Pair(newPrice, true) // Цена изменилась
            } else {
                Pair(null, false) // Цена осталась той же
            }
        }
    }
}