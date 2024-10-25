package org.example.command

import org.example.common.Context
import org.example.common.SubscriptionContext
import org.example.storage.models.Article

class SendMessageCommand (private val actualArticle: Article) : Command {

    override fun execute(context: Context) {
        println("To user: ${(context as SubscriptionContext).user}")
        println("Цена на товар ${context.subscription.article.name} изменилась!\n" +
                "Старая цена: ${context.subscription.article.price}\n" +
                "Новая цена: ${actualArticle.price}")
    }
}