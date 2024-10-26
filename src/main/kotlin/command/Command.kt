package org.example.command

import org.example.common.context.Context

interface Command {
    fun execute(context: Context)
}