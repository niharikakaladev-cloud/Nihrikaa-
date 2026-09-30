package com.example.data.repository

import com.example.R
import com.example.data.model.GameItem
import com.example.data.model.SpinRecord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GameRepository private constructor() {

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _remainingSpins = MutableStateFlow(5)
    val remainingSpins: StateFlow<Int> = _remainingSpins.asStateFlow()

    private val _spinHistory = MutableStateFlow<List<SpinRecord>>(emptyList())
    val spinHistory: StateFlow<List<SpinRecord>> = _spinHistory.asStateFlow()

    val gamesList: List<GameItem> = listOf(
        GameItem(
            id = "spin",
            title = "Lucky Wheel Spin",
            category = "Chance & Rewards",
            playersCount = "12.4K Playing",
            maxReward = "Win up to ₹500",
            iconResId = R.drawable.img_hero_banner
        ),
        GameItem(
            id = "ludo",
            title = "Ludo Classic",
            category = "Strategy Board Game",
            playersCount = "45.8K Playing",
            maxReward = "Free Play Demo",
            iconResId = R.drawable.img_games_showcase
        ),
        GameItem(
            id = "carrom",
            title = "Royal Carrom",
            category = "Physics Arcade",
            playersCount = "28.1K Playing",
            maxReward = "Queen Bonus",
            iconResId = R.drawable.img_games_showcase
        ),
        GameItem(
            id = "teenpatti",
            title = "Teen Patti Indian Poker",
            category = "Skill Card Game",
            playersCount = "52.3K Playing",
            maxReward = "Demo Chips Play",
            iconResId = R.drawable.img_cards_showcase
        ),
        GameItem(
            id = "dragontiger",
            title = "Dragon Tiger",
            category = "Quick Action Cards",
            playersCount = "39.6K Playing",
            maxReward = "Free Multiplier",
            iconResId = R.drawable.img_cards_showcase
        )
    )

    fun recordSpinResult(
        userId: String,
        amount: Double,
        label: String,
        onSuccess: (Double) -> Unit
    ) {
        if (_remainingSpins.value <= 0) return

        _remainingSpins.value = (_remainingSpins.value - 1).coerceAtLeast(0)

        val record = SpinRecord(
            id = "spin_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            rewardAmount = amount,
            rewardLabel = label,
            timestamp = System.currentTimeMillis()
        )

        _spinHistory.value = listOf(record) + _spinHistory.value

        if (amount > 0) {
            WalletRepository.getInstance().recordCreditReward(
                userId = userId,
                amount = amount,
                title = "Lucky Spin: $label",
                type = com.example.data.model.TransactionType.SPIN_REWARD
            )
        }

        if (firestore != null && userId.isNotBlank()) {
            firestore.collection("spin_history").document(record.id).set(record)
        }

        onSuccess(amount)
    }

    fun recordGameScore(userId: String, gameName: String, scoreOrOutcome: String) {
        if (firestore != null && userId.isNotBlank()) {
            val record = mapOf(
                "userId" to userId,
                "game" to gameName,
                "outcome" to scoreOrOutcome,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("game_results").add(record)
        }
    }

    companion object {
        @Volatile
        private var instance: GameRepository? = null

        fun getInstance(): GameRepository {
            return instance ?: synchronized(this) {
                instance ?: GameRepository().also { instance = it }
            }
        }
    }
}
