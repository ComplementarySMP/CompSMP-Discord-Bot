package io.github.arkosammy12.compsmpdiscordbot.extensions

import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.utils.respond
import io.github.arkosammy12.compsmpdiscordbot.CompSMPDiscordBot
import io.github.arkosammy12.compsmpdiscordbot.config.ConfigUtils
import io.github.arkosammy12.monkeyconfig.base.settings
import io.github.arkosammy12.monkeyconfig.managers.getStringMapSection
import io.github.arkosammy12.monkeyconfig.sections.maps.StringMapSection
import java.util.stream.Collectors

class ConfigurableChatCommandsExtension : Extension() {

    override val name: String = NAME

    val configurableCommands: () -> Map<String, String> = {
        val mapSection: StringMapSection = CompSMPDiscordBot.CONFIG_MANAGER.getStringMapSection(ConfigUtils.CHAT_COMMANDS)!!
        mapSection.settings.stream().collect(Collectors.toMap({ setting -> setting.name }, { setting -> setting.value.raw as String }))
    }

    override suspend fun setup() {
        for ((commandName, commandContent) in configurableCommands()) {
            chatCommand {
                name = Key(commandName)
                description = Key("This is a placeholder description for chat commands.")
                action {
                    message.respond(commandContent)
                }
            }

        }

    }

    companion object {
        const val NAME = "configurable_chat_commands_extension"
    }

}