package org.example.storage.dao

import org.example.parser.ShopName
import org.example.storage.models.Article
import org.example.storage.tables.Articles
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * Data Access Object (DAO) для работы с сущностью товара.
 *
 * @property id Идентификатор товара.
 */
class ArticleDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ArticleDao>(Articles)

    var price by Articles.price
    var name by Articles.name
    var shopName by Articles.shopName
    var url by Articles.url

    /**
     * Преобразует текущий объект DAO в объект товара.
     *
     * @return Экземпляр товара с данными из DAO.
     */
    fun toArticle() = Article(price, name, ShopName.valueOf(shopName), url)
}