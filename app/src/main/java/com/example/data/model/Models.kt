package com.example.data.model

import com.google.firebase.Timestamp

enum class AuthState {
    IDLE,
    CODE_SENT,
    VERIFYING,
    AUTHENTICATED,
    ERROR
}

data class UserProfile(
    val uid: String = "",
    val name: String = "Gamer",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val referralCode: String = "",
    val referredBy: String = "",
    val registrationDate: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false,
    val kycVerified: Boolean = false,
    val vipLevel: Int = 1
)

data class Wallet(
    val userId: String = "",
    val balance: Double = 50.0, // Welcome bonus
    val totalEarnings: Double = 50.0,
    val pendingWithdrawal: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class TransactionType {
    WELCOME_BONUS,
    DAILY_CHECK_IN,
    SPIN_REWARD,
    GAME_WIN,
    TASK_REWARD,
    REFERRAL_BONUS,
    WITHDRAWAL_REQUEST,
    WITHDRAWAL_REFUND,
    ADD_FUNDS
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED,
    REJECTED
}

data class TransactionRecord(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.WELCOME_BONUS,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: String = ""
)

enum class WithdrawalMethod {
    UPI,
    BANK_TRANSFER
}

enum class WithdrawalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class WithdrawalRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val amount: Double = 0.0,
    val method: WithdrawalMethod = WithdrawalMethod.UPI,
    val upiId: String = "",
    val accountNumber: String = "",
    val accountHolder: String = "",
    val ifscCode: String = "",
    val status: WithdrawalStatus = WithdrawalStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val adminNote: String = ""
)

data class SpinRecord(
    val id: String = "",
    val userId: String = "",
    val rewardAmount: Double = 0.0,
    val rewardLabel: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class EarningTask(
    val id: String,
    val title: String,
    val subtitle: String,
    val reward: Double,
    val progress: Int,
    val maxProgress: Int,
    val isCompleted: Boolean,
    val iconName: String
)

data class AppNotification(
    val id: String = "",
    val userId: String = "", // empty for broadcast to all
    val title: String = "",
    val message: String = "",
    val type: String = "ANNOUNCEMENT", // REWARD, WITHDRAWAL, ANNOUNCEMENT, SYSTEM
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)

data class GameItem(
    val id: String,
    val title: String,
    val category: String,
    val playersCount: String,
    val maxReward: String,
    val isLive: Boolean = true,
    val iconResId: Int = 0
)
