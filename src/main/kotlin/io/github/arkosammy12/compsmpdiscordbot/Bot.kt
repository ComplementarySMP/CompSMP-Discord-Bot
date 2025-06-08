package io.github.arkosammy12.compsmpdiscordbot

import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kordex.core.ExtensibleBot
import io.github.arkosammy12.compsmpdiscordbot.extensions.ApprovalExtension
import io.github.arkosammy12.compsmpdiscordbot.extensions.ConfigurableChatCommandsExtension
import kotlinx.coroutines.runBlocking

object Bot {

    fun createBot(token: String): ExtensibleBot {
        return runBlocking {
            ExtensibleBot(token) {
                chatCommands {
                    enabled = true
                    defaultPrefix = "!"
                }
                extensions {
                    add(::ApprovalExtension)
                    add(::ConfigurableChatCommandsExtension)
                }
                about {

                }
                hooks {

                }
                @OptIn(PrivilegedIntent::class)
                intents {
                    +Intent.GuildMembers
                    +Intent.GuildMessageReactions
                    +Intent.MessageContent
                }
                cache {

                }
            }
        }
    }

}