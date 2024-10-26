package org.example.storage.repository

import org.example.parser.ParserFactory
import org.example.parser.ShopName
import org.example.storage.dao.ArticleDao
import org.example.storage.dao.SubscriptionDao
import org.example.storage.dao.UserDao
import org.example.storage.models.Subscription
import org.example.storage.tables.Subscriptions
import org.jetbrains.exposed.sql.transactions.transaction


//class SubscriptionRepository {
//    private val parser = ParserFactory.make(ShopName.VINYLBOX)
//    private val subList = mutableListOf<Subscription>(
//        Subscription(
//            1, 1,
//            parser.getArticleInfo("http://www.vinylbox.ru/mainpage/240425163945240425163945240425163945240930143041240930143041240930143041241012145244241012145244241012145244"
//            ).apply {
//                this.price = BigDecimal((10000..20000).random())
//            },
//            createdTime = LocalDateTime.now(),
//            nextExecutionTime = LocalDateTime.now().plusSeconds(5)
//        ),
//        Subscription(
//            2, 1,
//            parser.getArticleInfo("http://www.vinylbox.ru/mainpage/240326192921240326192921240326192921").apply {
//                this.price = BigDecimal((10000..20000).random())
//            },
//            createdTime = LocalDateTime.now(),
//            nextExecutionTime = LocalDateTime.now().plusSeconds(5)
//        ),
//        Subscription(
//            3, 2,
//            parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/116225").apply {
//                this.price = BigDecimal((10000..20000).random())
//            },
//            createdTime = LocalDateTime.now(),
//            nextExecutionTime = LocalDateTime.now().plusSeconds(5)
//        ),
//        Subscription(
//            3, 2,
//            parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/120894").apply {
//                this.price = BigDecimal((10000..20000).random())
//            },
//            createdTime = LocalDateTime.now(),
//            nextExecutionTime = LocalDateTime.now().plusSeconds(5)
//        ),
//        Subscription(
//            4, 3,
//            parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/119164").apply {
//                this.price = BigDecimal((10000..20000).random())
//            },
//            createdTime = LocalDateTime.now(),
//            nextExecutionTime = LocalDateTime.now().plusSeconds(5)
//        ),
//        Subscription(
//            5, 1,
//            parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/125649").apply {
//                this.price = BigDecimal((10000..20000).random())
//            },
//            createdTime = LocalDateTime.now(),
//            nextExecutionTime = LocalDateTime.now().plusSeconds(5)
//        ))
////        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/125875"),
////        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/127320"),
////        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/125825"),
////        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/90551"),
////        parser.getArticleInfo("http://www.vinylbox.ru/vinyl-catalog/progefolk/product/view/48/111398")
//
//    fun getAllSubscriptions(): List<Subscription> {
//        return subList
//    }
//
//    fun getSubscriptions(userId: Long): List<Subscription> {
//        return subList.filter {
//            it.userId == userId
//        }
//    }
//
//    fun addSubscription(userId: Long, shopName: ShopName, url: String) {
//        val parser = ParserFactory.make(shopName)
//        subList.add(Subscription(
//            id = userId + (0L..10000L).random(),
//            userId = userId,
//            article = parser.getArticleInfo(url),
//            createdTime = LocalDateTime.now(),
//            nextExecutionTime = null
//        ))
//    }
//
//    fun removeSubscription(selectedSubscription: Long) {
//        subList.removeIf {
//            it.id == selectedSubscription
//        }
//    }
//}

class SubscriptionRepository {

    fun addSubscription(subscription: Subscription): Subscription = transaction {
        val userDao = UserDao.findById(subscription.userId)
            ?: throw IllegalArgumentException("User not found")
        val articleDao = ArticleDao.new {
            price = subscription.article.price
            name = subscription.article.name
            shopName = subscription.article.shopName.toString()
            url = subscription.article.url
        }
        val subscriptionDao = SubscriptionDao.new {
            user = userDao
            article = articleDao
            createdTime = subscription.createdTime
            nextExecutionTime = subscription.nextExecutionTime
        }
        subscriptionDao.toSubscription()
    }

    fun getSubscriptionById(id: Long): Subscription? = transaction {
        SubscriptionDao.findById(id)?.toSubscription()
    }

    fun getSubscriptionsByUserId(userId: Long): List<Subscription> = transaction {
        SubscriptionDao.find {
            Subscriptions.userId eq userId
        }.map {
            it.toSubscription()
        }
    }

    fun updateSubscription(subscription: Subscription): Boolean = transaction {
        val subscriptionDao = SubscriptionDao.findById(subscription.id) ?: return@transaction false
        subscriptionDao.article.price = subscription.article.price
        subscriptionDao.nextExecutionTime = subscription.nextExecutionTime
        true
    }

    fun deleteSubscription(id: Long): Boolean = transaction {
        val subscriptionDao = SubscriptionDao.findById(id) ?: return@transaction false
        subscriptionDao.delete()
        true
    }

    fun getAllSubscriptions(): List<Subscription> = transaction {
        SubscriptionDao.all().map { it.toSubscription() }
    }
}
