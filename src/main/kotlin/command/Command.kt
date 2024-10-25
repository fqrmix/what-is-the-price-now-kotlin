package org.example.command

import org.example.common.Context

interface Command {
    fun execute(context: Context)
}