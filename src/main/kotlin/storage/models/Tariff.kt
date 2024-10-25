package org.example.storage.models

enum class Tariff {
    STANDART {
        override fun getTimeOfUpdate(): Long {
            return 6
        }
    }, PREMIUM {
        override fun getTimeOfUpdate(): Long {
            return 3
        }
    }, ULTIMATE {
        override fun getTimeOfUpdate(): Long {
            return 1
        }
    };

    abstract fun getTimeOfUpdate(): Long

    companion object {

    }
}