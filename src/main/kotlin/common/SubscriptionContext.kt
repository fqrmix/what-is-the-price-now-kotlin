package org.example.common

import org.example.storage.models.Article
import org.example.storage.models.Subscription
import org.example.storage.models.User
import java.time.LocalDateTime

data class SubscriptionContext(
    val user: User,
    val subscription: Subscription
) : Context