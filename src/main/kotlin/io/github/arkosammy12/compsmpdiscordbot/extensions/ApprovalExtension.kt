package io.github.arkosammy12.compsmpdiscordbot.extensions

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.Member
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import dev.kord.rest.request.errorString
import dev.kord.rest.request.isError
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.types.Key
import io.github.arkosammy12.compsmpdiscordbot.CompSMPDiscordBot
import io.github.arkosammy12.compsmpdiscordbot.config.ConfigUtils
import io.github.arkosammy12.monkeyconfig.managers.getRawNumberSettingValue
import io.github.arkosammy12.monkeyconfig.managers.getRawStringSettingValue
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.filter
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

class ApprovalExtension(override val name: String) : Extension() {

    val adminRoleId: Long = CompSMPDiscordBot.CONFIG_MANAGER.getRawNumberSettingValue(ConfigUtils.COMPSMP_ADMIN_ROLE_ID)!!
    val applicationChannelId: Long = CompSMPDiscordBot.CONFIG_MANAGER.getRawNumberSettingValue(ConfigUtils.APPLICATION_CHANNEL_ID)!!
    val approvalEmojiId: Long = CompSMPDiscordBot.CONFIG_MANAGER.getRawNumberSettingValue(ConfigUtils.APPROVAL_EMOJI_ID)!!
    val approvalRoleId: Long = CompSMPDiscordBot.CONFIG_MANAGER.getRawNumberSettingValue(ConfigUtils.APPROVAL_ROLE_ID)!!
    val approvalEmojiName: String = CompSMPDiscordBot.CONFIG_MANAGER.getRawStringSettingValue(ConfigUtils.APPROVAL_EMOJI_NAME)!!

    override suspend fun setup() {
        this.event<ReactionAddEvent> {
            action {
                if (!matchesApprovalContext(event.emoji, event.user, event.message)) {
                    return@action
                }
                val applicant: Member = event.messageAuthor?.asMember(CompSMPDiscordBot.guildSnowFlake) ?: return@action
                applicant.addRole(Snowflake(approvalRoleId))
            }

        }
        this.event<ReactionRemoveEvent> {
            action {
                if (!matchesApprovalContext(event.emoji, event.user, event.message)) {
                    return@action
                }
                val applicant: Member = event.message.asMessage().author?.asMember(CompSMPDiscordBot.guildSnowFlake) ?: return@action
                val applicantRoles: Set<Snowflake> = applicant.roleIds
                if (!applicantRoles.any { roleId -> roleId.value.toLong() == approvalRoleId }) {
                    return@action
                }
                val reactionEmoji: ReactionEmoji = ReactionEmoji.Custom(Snowflake(approvalEmojiId), approvalEmojiName, false)
                var hasApprovedRoleByOtherAdmin = false
                event.message.getReactors(reactionEmoji)
                event.message.getReactors(reactionEmoji).filter { user -> user.id != event.user }.collect { user ->
                    val member: Member = user.asMember(CompSMPDiscordBot.guildSnowFlake)
                    if (member.roleIds.any {roleId -> roleId.value.toLong() == adminRoleId}) {
                        hasApprovedRoleByOtherAdmin = true
                        return@collect
                    }
                }
                if (!hasApprovedRoleByOtherAdmin) {
                    applicant.removeRole(Snowflake(approvalRoleId))
                }
            }
        }
        publicSlashCommand(::FetchProfileArguments) {
            name = Key("fetchGameProfile")
            description = Key("Fetches a gameprofile for a given Minecraft username")
            guild(CompSMPDiscordBot.guildSnowFlake)
            action {
                respond {
                    val username: String = arguments.username
                    val client = HttpClient()
                    val response: HttpResponse = client.get(URLBuilder("https://api.mojang.com/users/profiles/minecraft/${username}").build()) {
                        method = HttpMethod.Get
                        timeout {
                            requestTimeoutMillis = 5_000
                        }
                    }
                    if (response.status == HttpStatusCode.OK) {
                        val json: JsonObject = Json.parseToJsonElement(response.body()).jsonObject
                        val rawUuid: String? = json["id"]?.jsonPrimitive?.content
                        if (rawUuid == null) {
                            content = "Error deserializing uuid. Missing field \"id\" from json response!"
                        } else {
                            val uuid: UUID = UUID.fromString(addDashesToUuid(rawUuid))
                            content = "Username: `${username}`. UUID: `$uuid`"
                        }
                    } else if (response.isError) {
                        content = "Error fetching game profile: ${response.errorString()}"
                    } else {
                        content = "Error fetching game profile: ${response.status}"
                    }
                    client.close()
                }
            }
        }

    }

    private suspend fun matchesApprovalContext(eventEmoji: ReactionEmoji, user: UserBehavior, message: MessageBehavior): Boolean {
        if (eventEmoji !is ReactionEmoji.Custom) {
            return false
        }
        val reactor: Member = user.asMember(CompSMPDiscordBot.guildSnowFlake)
        if (!reactor.roleIds.any { roleId -> roleId.value.toLong() == adminRoleId }) {
            return false
        }
        if (message.channelId.value.toLong() != applicationChannelId) {
            return false
        }
        if (eventEmoji.id.value.toLong() != approvalEmojiId) {
            return false
        }
        return true
    }

    private fun addDashesToUuid(uuid: String): String {
        val buffer: StringBuffer = StringBuffer(uuid)
        buffer.insert(20, '-')
        buffer.insert(16, '-')
        buffer.insert(12, '-')
        buffer.insert(8, '-')
        return buffer.toString()
    }

    inner class FetchProfileArguments() : Arguments() {
        val username by string {
            name = Key("username")
            description = Key("The username of the person you wish to see the Minecraft game profile of.")
        }
    }

}