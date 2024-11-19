package org.example.common.state

interface UserState {
    enum class State {
        NONE
    }
    fun nextState(): UserState
}