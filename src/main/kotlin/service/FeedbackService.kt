package org.example.service

import org.example.storage.models.FeedbackMessage
import org.example.storage.repository.FeedbackMessageRepository

class FeedbackService {

    private val feedbackMessageRepository = FeedbackMessageRepository()

    fun getUserFeedbackMessages(userId: Long): List<FeedbackMessage> {
        return feedbackMessageRepository.getFeedbackMessagesByUserId(userId)
    }

    fun getFeedbackMessage(feedbackMessageId: Long): FeedbackMessage? {
        return feedbackMessageRepository.getFeedbackMessageById(feedbackMessageId)
    }

    fun addFeedbackMessage(feedbackMessage: FeedbackMessage): FeedbackMessage {
        return feedbackMessageRepository.addFeedbackMessage(feedbackMessage)
    }

}