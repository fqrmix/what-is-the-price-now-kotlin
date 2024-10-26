package org.example.parser

import org.example.storage.models.Article
import java.net.URL


/**
 * Интерфейс для реализации парсеров, которые извлекают информацию о товарах с различных сайтов.
 */
interface Parser {

    /**
     * Флаг, указывающий, нужно ли обновлять информацию о товаре при каждом запросе.
     * Если `true`, каждый вызов метода [getArticleInfo] будет повторно загружать данные о товаре.
     */
    val forceUpdate: Boolean

    /**
     * Возвращает информацию о товаре, извлеченную с указанного URL.
     *
     * @param articleUrl URL страницы товара.
     * @return Объект [Article], содержащий актуальные данные о товаре, такие как цена и название.
     * @throws Exception если не удается получить информацию о товаре с указанной страницы.
     */
    fun getArticleInfo(articleUrl: URL): Article
}
