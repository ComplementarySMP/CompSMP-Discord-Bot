package io.github.arkosammy12.compsmpdiscordbot.extensions

import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.toKey
import io.github.arkosammy12.compsmpdiscordbot.CompSMPDiscordBot
import io.github.arkosammy12.compsmpdiscordbot.config.ConfigUtils
import io.github.arkosammy12.monkeyconfig.base.settings
import io.github.arkosammy12.monkeyconfig.managers.getStringMapSection
import io.github.arkosammy12.monkeyconfig.sections.maps.StringMapSection
import java.util.stream.Collectors

class ReloadableChatCommandsExtension : Extension() {

    override val name: String = NAME

    val configurableCommands: () -> Map<String, String>
        get() = {
            val mapSection: StringMapSection = CompSMPDiscordBot.CONFIG_MANAGER.getStringMapSection(ConfigUtils.CHAT_COMMANDS)!!
            mapSection.settings.stream().collect(Collectors.toMap({ setting -> setting.name }, { setting -> setting.value.raw as String }))
        }

    override suspend fun setup() {
        for ((commandName, commandContent) in configurableCommands()) {
            publicSlashCommand {
                name = commandName.toKey()
                description = CoreTranslations.Commands.defaultDescription
                action {
                    respond {
                        content = commandContent
                    }
                }
            }

        }

    }

    companion object {
        const val NAME = "reloadable_chat_commands_extension"
    }

}