package org.example.common.context

import org.example.common.context.Context
import org.example.storage.models.Article
import org.example.storage.models.Subscription
import org.example.storage.models.User

data class SubscriptionContext(
    val user: User?,
    val subscription: Subscription?,
    var article: Article?
) : Context