package org.example.common.state

enum class NotificationUserState: UserState {
    NONE {
        override fun nextState(): NotificationUserState {
            return NOTIFICATION_AWAITING_TIME_TO_NOTIFY
        }
    },
    NOTIFICATION_AWAITING_TIME_TO_NOTIFY {
        override fun nextState(): NotificationUserState {
            return NONE
        }
    };

    abstract override fun nextState(): NotificationUserState
}