package com.example.data.repository

import android.util.Log
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.Wallet
import com.example.data.model.WithdrawalMethod
import com.example.data.model.WithdrawalRequest
import com.example.data.model.WithdrawalStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class WalletRepository private constructor() {

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.w("WalletRepository", "Firestore not initialized: ${e.message}")
        null
    }

    private val _wallet = MutableStateFlow(Wallet())
    val wallet: StateFlow<Wallet> = _wallet.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val withdrawals: StateFlow<List<WithdrawalRequest>> = _withdrawals.asStateFlow()

    private val _allWithdrawalsForAdmin = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val allWithdrawalsForAdmin: StateFlow<List<WithdrawalRequest>> = _allWithdrawalsForAdmin.asStateFlow()

    init {
        // Initialize starter transactions for first launch preview
        val initialTx = listOf(
            TransactionRecord(
                id = "tx_welcome_1",
                userId = "default",
                title = "Welcome Signup Bonus",
                amount = 50.0,
                type = TransactionType.WELCOME_BONUS,
                status = TransactionStatus.COMPLETED,
                timestamp = System.currentTimeMillis() - 86400000L
            )
        )
        _transactions.value = initialTx
    }

    fun syncUserWallet(userId: String) {
        if (userId.isBlank()) return

        if (firestore != null) {
            val walletDoc = firestore.collection("wallets").document(userId)
            walletDoc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("WalletRepository", "Listen failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val balance = snapshot.getDouble("balance") ?: 50.0
                    val total = snapshot.getDouble("totalEarnings") ?: 50.0
                    val pending = snapshot.getDouble("pendingWithdrawal") ?: 0.0
                    _wallet.value = Wallet(
                        userId = userId,
                        balance = balance,
                        totalEarnings = total,
                        pendingWithdrawal = pending,
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    // Create default wallet on first signup
                    val defaultWallet = hashMapOf(
                        "userId" to userId,
                        "balance" to 50.0,
                        "totalEarnings" to 50.0,
                        "pendingWithdrawal" to 0.0,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                    walletDoc.set(defaultWallet)
                }
            }

            // Sync user transactions
            firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshots, err ->
                    if (err != null) return@addSnapshotListener
                    val list = snapshots?.documents?.mapNotNull { doc ->
                        try {
                            TransactionRecord(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                title = doc.getString("title") ?: "Reward",
                                amount = doc.getDouble("amount") ?: 0.0,
                                type = TransactionType.valueOf(doc.getString("type") ?: "TASK_REWARD"),
                                status = TransactionStatus.valueOf(doc.getString("status") ?: "COMPLETED"),
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    if (list.isNotEmpty()) {
                        _transactions.value = list
                    }
                }

            // Sync user withdrawals
            firestore.collection("withdrawals")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, err ->
                    if (err != null) return@addSnapshotListener
                    val list = snapshots?.documents?.mapNotNull { doc ->
                        parseWithdrawal(doc.id, doc.data)
                    } ?: emptyList()
                    _withdrawals.value = list
                }
        }
    }

    fun syncAllWithdrawalsForAdmin() {
        if (firestore != null) {
            firestore.collection("withdrawals")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, err ->
                    if (err != null) return@addSnapshotListener
                    val list = snapshots?.documents?.mapNotNull { doc ->
                        parseWithdrawal(doc.id, doc.data)
                    } ?: emptyList()
                    _allWithdrawalsForAdmin.value = list
                }
        }
    }

    private fun parseWithdrawal(id: String, data: Map<String, Any>?): WithdrawalRequest? {
        if (data == null) return null
        return try {
            WithdrawalRequest(
                id = id,
                userId = data["userId"] as? String ?: "",
                userName = data["userName"] as? String ?: "",
                userPhone = data["userPhone"] as? String ?: "",
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                method = WithdrawalMethod.valueOf(data["method"] as? String ?: "UPI"),
                upiId = data["upiId"] as? String ?: "",
                accountNumber = data["accountNumber"] as? String ?: "",
                accountHolder = data["accountHolder"] as? String ?: "",
                ifscCode = data["ifscCode"] as? String ?: "",
                status = WithdrawalStatus.valueOf(data["status"] as? String ?: "PENDING"),
                timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis(),
                adminNote = data["adminNote"] as? String ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    fun recordCreditReward(
        userId: String,
        amount: Double,
        title: String,
        type: TransactionType
    ) {
        val newBalance = _wallet.value.balance + amount
        val newEarnings = _wallet.value.totalEarnings + amount
        _wallet.value = _wallet.value.copy(
            balance = newBalance,
            totalEarnings = newEarnings,
            lastUpdated = System.currentTimeMillis()
        )

        val tx = TransactionRecord(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            title = title,
            amount = amount,
            type = type,
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis()
        )
        _transactions.value = listOf(tx) + _transactions.value

        if (firestore != null && userId.isNotBlank()) {
            val walletDoc = firestore.collection("wallets").document(userId)
            walletDoc.update(
                mapOf(
                    "balance" to newBalance,
                    "totalEarnings" to newEarnings,
                    "lastUpdated" to System.currentTimeMillis()
                )
            )
            firestore.collection("transactions").document(tx.id).set(tx)
        }
    }

    fun submitWithdrawal(
        userId: String,
        userName: String,
        userPhone: String,
        amount: Double,
        method: WithdrawalMethod,
        upiId: String = "",
        accountNumber: String = "",
        accountHolder: String = "",
        ifscCode: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        if (amount < 100.0) {
            onResult(false, "Minimum withdrawal is ₹100.00")
            return
        }
        if (_wallet.value.balance < amount) {
            onResult(false, "Insufficient balance! Available: ₹${String.format("%.2f", _wallet.value.balance)}")
            return
        }

        // Deduct from balance and move to pending withdrawal
        val newBalance = _wallet.value.balance - amount
        val newPending = _wallet.value.pendingWithdrawal + amount
        _wallet.value = _wallet.value.copy(
            balance = newBalance,
            pendingWithdrawal = newPending,
            lastUpdated = System.currentTimeMillis()
        )

        val requestId = "wdr_${UUID.randomUUID().toString().take(8)}"
        val req = WithdrawalRequest(
            id = requestId,
            userId = userId,
            userName = userName,
            userPhone = userPhone,
            amount = amount,
            method = method,
            upiId = upiId,
            accountNumber = accountNumber,
            accountHolder = accountHolder,
            ifscCode = ifscCode,
            status = WithdrawalStatus.PENDING,
            timestamp = System.currentTimeMillis()
        )
        _withdrawals.value = listOf(req) + _withdrawals.value
        _allWithdrawalsForAdmin.value = listOf(req) + _allWithdrawalsForAdmin.value

        val tx = TransactionRecord(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            title = "Withdrawal to ${if (method == WithdrawalMethod.UPI) "UPI: $upiId" else "Bank: $accountNumber"}",
            amount = amount,
            type = TransactionType.WITHDRAWAL_REQUEST,
            status = TransactionStatus.PENDING,
            timestamp = System.currentTimeMillis()
        )
        _transactions.value = listOf(tx) + _transactions.value

        if (firestore != null && userId.isNotBlank()) {
            firestore.collection("wallets").document(userId).update(
                mapOf(
                    "balance" to newBalance,
                    "pendingWithdrawal" to newPending,
                    "lastUpdated" to System.currentTimeMillis()
                )
            )
            firestore.collection("withdrawals").document(requestId).set(req)
            firestore.collection("transactions").document(tx.id).set(tx)
        }

        onResult(true, "Withdrawal request of ₹${String.format("%.2f", amount)} submitted successfully! Status: PENDING validation.")
    }

    fun updateWithdrawalStatusByAdmin(
        requestId: String,
        newStatus: WithdrawalStatus,
        adminNote: String
    ) {
        val currentList = _allWithdrawalsForAdmin.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == requestId }
        if (index != -1) {
            val oldItem = currentList[index]
            val updatedItem = oldItem.copy(status = newStatus, adminNote = adminNote)
            currentList[index] = updatedItem
            _allWithdrawalsForAdmin.value = currentList

            // Also update in user's view if it's the current user
            val userList = _withdrawals.value.toMutableList()
            val userIndex = userList.indexOfFirst { it.id == requestId }
            if (userIndex != -1) {
                userList[userIndex] = updatedItem
                _withdrawals.value = userList

                // If rejected, refund pending amount back to balance
                if (newStatus == WithdrawalStatus.REJECTED && oldItem.status == WithdrawalStatus.PENDING) {
                    val refundedBalance = _wallet.value.balance + oldItem.amount
                    val reducedPending = (_wallet.value.pendingWithdrawal - oldItem.amount).coerceAtLeast(0.0)
                    _wallet.value = _wallet.value.copy(
                        balance = refundedBalance,
                        pendingWithdrawal = reducedPending
                    )
                } else if (newStatus == WithdrawalStatus.APPROVED && oldItem.status == WithdrawalStatus.PENDING) {
                    val reducedPending = (_wallet.value.pendingWithdrawal - oldItem.amount).coerceAtLeast(0.0)
                    _wallet.value = _wallet.value.copy(
                        pendingWithdrawal = reducedPending
                    )
                }
            }

            if (firestore != null) {
                firestore.collection("withdrawals").document(requestId).update(
                    mapOf(
                        "status" to newStatus.name,
                        "adminNote" to adminNote
                    )
                )
            }
        }
    }

    companion object {
        @Volatile
        private var instance: WalletRepository? = null

        fun getInstance(): WalletRepository {
            return instance ?: synchronized(this) {
                instance ?: WalletRepository().also { instance = it }
            }
        }
    }
}
