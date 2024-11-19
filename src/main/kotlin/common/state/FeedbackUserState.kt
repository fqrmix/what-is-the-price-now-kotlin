package org.example.common.state

enum class FeedbackUserState: UserState {
    NONE {
        override fun nextState(): FeedbackUserState{
            return FEEDBACK_AWAITING_MESSAGE
        }
    },
    FEEDBACK_AWAITING_MESSAGE {
        override fun nextState(): FeedbackUserState {
            return NONE
        }
    };

    abstract override fun nextState(): FeedbackUserState
}