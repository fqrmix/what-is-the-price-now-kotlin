package org.example.command

import org.example.common.context.Context
import org.example.common.context.SubscriptionContext
import org.example.storage.models.Article
import org.example.storage.models.Subscription
import java.net.URL
import java.time.LocalDateTime


class ComparePriceCommand {

    fun execute(subscription: Subscription): Boolean {
        val currentPrice = subscription.article.price
        val actualArticleInfo = getArticleInfo(URL(subscription.article.url))
        return if (actualArticleInfo != null) {
            currentPrice > actualArticleInfo.price
        } else false
    }

    private fun getArticleInfo(url: URL): Article? {
        return ParseArticleCommand().execute(url)
    }

    private fun setNewExecutionTime(context: SubscriptionContext) {
        context.subscription!!.nextExecutionTime =
            LocalDateTime.now().plusHours(context.user!!.tariff.getTimeOfUpdate())
    }
}
