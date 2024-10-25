package org.example.command

import org.example.common.Context
import org.example.common.SubscriptionContext
import org.example.storage.models.Article
import org.example.parser.ParserFactory
import java.time.LocalDateTime


class CheckPriceCommand: Command {

    override fun execute(context: Context) {
        if (context is SubscriptionContext) {
            val actualArticle = getActualArticleInfo(context)
            if (context.subscription.article.price > actualArticle.price) {
                SendMessageCommand(actualArticle).execute(context)
                context.subscription.article.price = actualArticle.price
            }
            setNewExecutionTime(context)
        }
    }

    private fun getActualArticleInfo(context: SubscriptionContext): Article {
        val parser = ParserFactory.make(context.subscription.article.shopName)
        return parser.getArticleInfo(context.subscription.article.url)
    }

    private fun setNewExecutionTime(context: SubscriptionContext) {
        context.subscription.nextExecutionTime =
            LocalDateTime.now().plusHours(context.user.tariff.getTimeOfUpdate())
    }
}
