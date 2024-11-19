package org.example.common.state

enum class ArticleUserState: UserState {
    NONE {
        override fun nextState(): ArticleUserState {
            return ARTICLE_AWAITING_TIME_TO_NOTIFY
        }
    },
    ARTICLE_AWAITING_TIME_TO_NOTIFY {
        override fun nextState(): ArticleUserState {
            return ARTICLE_AWAITING_LINK
        }
    },
    ARTICLE_AWAITING_LINK {
        override fun nextState(): ArticleUserState {
            return NONE
        }
    };

    abstract override fun nextState(): ArticleUserState
}