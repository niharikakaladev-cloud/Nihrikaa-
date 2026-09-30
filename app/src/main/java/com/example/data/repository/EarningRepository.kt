package com.example.data.repository

import com.example.data.model.AppNotification
import com.example.data.model.EarningTask
import com.example.data.model.TransactionType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class EarningRepository private constructor() {

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _tasks = MutableStateFlow<List<EarningTask>>(emptyList())
    val tasks: StateFlow<List<EarningTask>> = _tasks.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _referralsCount = MutableStateFlow(3)
    val referralsCount: StateFlow<Int> = _referralsCount.asStateFlow()

    private val _referralEarnings = MutableStateFlow(75.0)
    val referralEarnings: StateFlow<Double> = _referralEarnings.asStateFlow()

    init {
        _tasks.value = listOf(
            EarningTask(
                id = "task_daily_login",
                title = "Daily Check-in Bonus",
                subtitle = "Claim your daily gamer attendance coin",
                reward = 10.0,
                progress = 1,
                maxProgress = 1,
                isCompleted = false,
                iconName = "check_circle"
            ),
            EarningTask(
                id = "task_spins",
                title = "Lucky Wheel Master",
                subtitle = "Complete 3 spins today",
                reward = 15.0,
                progress = 1,
                maxProgress = 3,
                isCompleted = false,
                iconName = "autorenew"
            ),
            EarningTask(
                id = "task_play_ludo",
                title = "Ludo Board Challenger",
                subtitle = "Play 1 complete round of Ludo demo",
                reward = 20.0,
                progress = 0,
                maxProgress = 1,
                isCompleted = false,
                iconName = "sports_esports"
            ),
            EarningTask(
                id = "task_refer_friend",
                title = "Squad Referral Boost",
                subtitle = "Invite 1 friend with your unique code",
                reward = 25.0,
                progress = 0,
                maxProgress = 1,
                isCompleted = false,
                iconName = "group_add"
            ),
            EarningTask(
                id = "task_partner_offer",
                title = "Explore Partner Gaming App",
                subtitle = "Authorized partner engagement task placeholder",
                reward = 30.0,
                progress = 0,
                maxProgress = 1,
                isCompleted = false,
                iconName = "verified"
            )
        )

        _notifications.value = listOf(
            AppNotification(
                id = "notif_1",
                title = "Welcome to Niharika Games!",
                message = "₹50 signup bonus credited directly into your wallet. Play safe demo games & claim rewards!",
                type = "REWARD",
                timestamp = System.currentTimeMillis() - 7200000L
            ),
            AppNotification(
                id = "notif_2",
                title = "Weekly Ludo Championship",
                message = "Compete with friends in Ludo Classic and win exclusive badges and coins.",
                type = "ANNOUNCEMENT",
                timestamp = System.currentTimeMillis() - 18000000L
            ),
            AppNotification(
                id = "notif_3",
                title = "Fast Withdrawal Update",
                message = "UPI withdrawals are processed 24x7 within 30 minutes after verification.",
                type = "SYSTEM",
                timestamp = System.currentTimeMillis() - 86400000L
            )
        )
    }

    fun claimTaskReward(userId: String, taskId: String, onResult: (Boolean, String) -> Unit) {
        val task = _tasks.value.find { it.id == taskId }
        if (task == null) {
            onResult(false, "Task not found")
            return
        }
        if (task.isCompleted) {
            onResult(false, "Task already claimed today!")
            return
        }
        if (task.progress < task.maxProgress) {
            onResult(false, "Task requirements not yet finished (${task.progress}/${task.maxProgress})")
            return
        }

        // Mark completed
        _tasks.value = _tasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = true) else it
        }

        // Credit to wallet
        WalletRepository.getInstance().recordCreditReward(
            userId = userId,
            amount = task.reward,
            title = "Task Completed: ${task.title}",
            type = TransactionType.TASK_REWARD
        )

        onResult(true, "Congratulations! ₹${task.reward} credited to your wallet.")
    }

    fun addBroadcastNotification(title: String, message: String, type: String = "ANNOUNCEMENT") {
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(notif) + _notifications.value

        if (firestore != null) {
            firestore.collection("notifications").document(notif.id).set(notif)
        }
    }

    companion object {
        @Volatile
        private var instance: EarningRepository? = null

        fun getInstance(): EarningRepository {
            return instance ?: synchronized(this) {
                instance ?: EarningRepository().also { instance = it }
            }
        }
    }
}
