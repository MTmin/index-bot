package com.tgse.index.provider

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import com.tgse.index.bot.Group
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * 信息来源分水岭
 * 区分 群组、私聊、管理 信息来源
 */
@Service
class WatershedProvider(
    private val botProvider: BotProvider,
    @Value("\${group.approve.id}")
    private val approveGroupChatId: Long
) {

    open class BotRequest(
        open val chatId: Long?,
        open val chatType: Chat.Type?,
        open val messageId: Int?,
        open val update: Update
    )

    class BotPrivateRequest(
        override val chatId: Long,
        override val messageId: Int?,
        override val update: Update
    ) : BotRequest(
        chatId,
        Chat.Type.Private,
        messageId,
        update
    )

    class BotGroupRequest(
        override val chatId: Long,
        override val messageId: Int?,
        override val update: Update
    ) : BotRequest(
        chatId,
        Chat.Type.group,
        messageId,
        update
    )

    class BotApproveRequest(
        override val chatId: Long,
        override val messageId: Int?,
        override val update: Update
    ) : BotRequest(
        chatId,
        Chat.Type.group,
        messageId,
        update
    )

    private val logger = LoggerFactory.getLogger(Group::class.java)
    private val requestSubject = BehaviorSubject.create<BotRequest>()
    val requestObservable: Observable<BotRequest> = requestSubject.distinct()

    init {
        subscribeUpdate()
    }

    private fun subscribeUpdate() {
        botProvider.updateObservable.subscribe(
            { update ->
                // 仅处理文字信息或按钮回执
                val messageContentIsNull = update.message() == null || update.message().text() == null
                if (messageContentIsNull && update.callbackQuery() == null) return@subscribe

                // 获取概要内容
                val callbackQuery = update.callbackQuery()

                val chatType = when (true) {
                    update.message() == null && callbackQuery == null -> null
                    update.message() != null -> update.message().chat().type()
                    callbackQuery != null -> callbackQuery.message().chat().type()
                    else -> null
                }

                val chatId = when (true) {
                    update.message() == null && callbackQuery == null -> null
                    callbackQuery?.message() != null -> callbackQuery.message().chat().id()
                    callbackQuery != null -> callbackQuery.from().id().toLong()
                    update.message() != null -> update.message().chat().id()
                    else -> null
                }

                val messageId = callbackQuery?.message()?.messageId()

                // send next
                when (chatType) {
                    Chat.Type.Private -> {
                        val request = BotPrivateRequest(chatId!!, messageId, update)
                        requestSubject.onNext(request)
                    }
                    Chat.Type.supergroup, Chat.Type.group -> {
                        val request =
                            if (chatId == approveGroupChatId) BotApproveRequest(chatId, messageId, update)
                            else BotGroupRequest(chatId!!, messageId, update)
                        requestSubject.onNext(request)
                    }
                    else -> {
                    }
                }
            },
            { throwable ->
                throwable.printStackTrace()
                logger.error("Group.error")
                botProvider.sendErrorMessage(throwable)
            },
            {
                logger.error("Group.complete")
            }
        )
    }

}