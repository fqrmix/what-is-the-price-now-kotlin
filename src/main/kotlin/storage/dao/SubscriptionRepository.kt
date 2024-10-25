package org.example.storage.dao

import org.example.parser.ParserFactory
import org.example.parser.ShopName
import org.example.storage.models.Article
import org.example.storage.models.Subscription
import java.math.BigDecimal

class SubscriptionRepository {
    private val parser = ParserFactory.make(ShopName.VINYLBOX)
    private val subList = mutableListOf(
        Subscription(
            1, 1,
            parser.getArticleInfo("http://www.vinylbox.ru/mainpage/240425163945240425163945240425163945240930143041240930143041240930143041241012145244241012145244241012145244"
            ).apply {
                this.price = BigDecimal((10000..20000).random())
            }
        ),
        Subscription(
            2, 1,
            parser.getArticleInfo("http://www.vinylbox.ru/mainpage/240326192921240326192921240326192921").apply {
                this.price = BigDecimal((10000..20000).random())
            }
        ),
        Subscription(
            3, 2,
            parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/116225").apply {
                this.price = BigDecimal((10000..20000).random())
            }
        ),
        Subscription(
            3, 2,
            parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/120894").apply {
                this.price = BigDecimal((10000..20000).random())
            }
        ),
        Subscription(
            4, 3,
            parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/119164").apply {
                this.price = BigDecimal((10000..20000).random())
            }
        ),
        Subscription(
            5, 1,
            parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/125649").apply {
                this.price = BigDecimal((10000..20000).random())
            }
        ),
//        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/125875"),
//        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/127320"),
//        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/125825"),
//        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/90551"),
//        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/111398")
    )

    fun getAllSubscriptions(): List<Subscription> {
        return subList
    }

    fun getSubscriptions(userId: Long): List<Subscription> {
        return subList.filter {
            it.userId == userId
        }
    }

    fun addSubscription(userId: Long, shopName: ShopName, url: String) {
        val parser = ParserFactory.make(shopName)
        subList.add(Subscription(
            id = userId + (0L..10000L).random(),
            userId = userId,
            article = parser.getArticleInfo(url)
        ))
    }

    fun removeSubscription(selectedSubscription: Long) {
        subList.removeIf {
            it.id == selectedSubscription
        }
    }
}