package com.example.ui.screens.wallet

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalMethod
import com.example.data.model.WithdrawalRequest
import com.example.data.model.WithdrawalStatus
import com.example.data.repository.WalletRepository
import com.example.ui.components.GamingCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingCardSurface
import com.example.ui.theme.GamingCardSurfaceElevated
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingEmerald
import com.example.ui.theme.GamingGold
import com.example.ui.theme.GamingRed
import com.example.ui.theme.GamingTextMuted
import com.example.ui.theme.GamingTextPrimary
import com.example.ui.theme.GamingTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val walletRepo = remember { WalletRepository.getInstance() }
    val wallet by walletRepo.wallet.collectAsState()
    val transactions by walletRepo.transactions.collectAsState()
    val withdrawals by walletRepo.withdrawals.collectAsState()

    var showWithdrawalDialog by remember { mutableStateOf(false) }
    var showAddFundsInfoDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Transactions, 1: Withdrawals

    Surface(
        color = GamingDarkBackground,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Title
                Column {
                    Text(
                        text = "Wallet & Passbook",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GamingTextPrimary
                    )
                    Text(
                        text = "Secure balances, validated payout requests & transaction audit trail",
                        fontSize = 12.sp,
                        color = GamingTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wallet Master Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF280E17), Color(0xFF1B1728), Color(0xFF100F17))
                            )
                        )
                        .border(1.dp, GamingRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AVAILABLE WITHDRAWABLE BALANCE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GamingTextSecondary,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${String.format("%.2f", wallet.balance)}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GamingGold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GamingGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = GamingGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Secondary Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x66000000))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Lifetime Earnings", fontSize = 10.sp, color = GamingTextMuted)
                                Text(
                                    text = "₹${String.format("%.2f", wallet.totalEarnings)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GamingEmerald
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Pending Payout", fontSize = 10.sp, color = GamingTextMuted)
                                Text(
                                    text = "₹${String.format("%.2f", wallet.pendingWithdrawal)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (wallet.pendingWithdrawal > 0) GamingGold else GamingTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showAddFundsInfoDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GamingCardSurfaceElevated),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                                    .testTag("wallet_add_funds_button")
                            ) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = GamingGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Funds", fontSize = 13.sp, color = GamingTextPrimary, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { showWithdrawalDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("wallet_withdraw_button")
                            ) {
                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Withdraw", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Tab Selector: Transactions vs Withdrawals
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = GamingCardSurface,
                    contentColor = GamingRed,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GamingRed
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "All Transactions (${transactions.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) GamingRed else GamingTextMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Withdrawals (${withdrawals.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) GamingRed else GamingTextMuted
                            )
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                // Transactions List
                if (transactions.isEmpty()) {
                    item {
                        EmptyListState(message = "No transactions yet. Win games and complete tasks!")
                    }
                } else {
                    items(transactions) { tx ->
                        TransactionItemCard(tx = tx)
                    }
                }
            } else {
                // Withdrawals List
                if (withdrawals.isEmpty()) {
                    item {
                        EmptyListState(message = "No withdrawal requests yet. Request your first withdrawal above (min ₹100).")
                    }
                } else {
                    items(withdrawals) { req ->
                        WithdrawalItemCard(request = req)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Withdrawal Request Form Dialog
        if (showWithdrawalDialog) {
            WithdrawalRequestDialog(
                availableBalance = wallet.balance,
                userName = userProfile.name,
                userPhone = userProfile.phone,
                onDismiss = { showWithdrawalDialog = false },
                onSubmit = { amount, method, upiId, accNum, holder, ifsc ->
                    walletRepo.submitWithdrawal(
                        userId = userProfile.uid,
                        userName = userProfile.name,
                        userPhone = userProfile.phone,
                        amount = amount,
                        method = method,
                        upiId = upiId,
                        accountNumber = accNum,
                        accountHolder = holder,
                        ifscCode = ifsc,
                        onResult = { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                showWithdrawalDialog = false
                            }
                        }
                    )
                }
            )
        }

        // Add Funds Info Dialog (Transparency: No fake payments)
        if (showAddFundsInfoDialog) {
            AlertDialog(
                onDismissRequest = { showAddFundsInfoDialog = false },
                containerColor = GamingCardSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = GamingGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Funds Gateway", color = GamingTextPrimary)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Payment Gateway Integration Notice:",
                            fontWeight = FontWeight.Bold,
                            color = GamingGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "In accordance with safe gaming guidelines, deposit buttons are disabled until an authorized Indian payment aggregator (such as Razorpay, Cashfree, or PayU) is verified and configured with merchant credentials on your backend.",
                            color = GamingTextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You can earn 100% free with Daily Bonuses, Spin Wheel rewards, and Squad Referrals!",
                            fontSize = 12.sp,
                            color = GamingEmerald
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAddFundsInfoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed)
                    ) {
                        Text("Got It", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun TransactionItemCard(tx: TransactionRecord) {
    val isCredit = tx.amount > 0 && tx.type != TransactionType.WITHDRAWAL_REQUEST
    GamingCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isCredit) GamingEmerald.copy(alpha = 0.15f) else GamingRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isCredit) GamingEmerald else GamingRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tx.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GamingTextPrimary
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp)),
                        fontSize = 11.sp,
                        color = GamingTextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"}₹${String.format("%.2f", tx.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) GamingEmerald else GamingRed
                )
                StatusBadge(status = tx.status.name)
            }
        }
    }
}

@Composable
fun WithdrawalItemCard(request: WithdrawalRequest) {
    GamingCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (request.method == WithdrawalMethod.UPI) Icons.Default.QrCode else Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = GamingGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (request.method == WithdrawalMethod.UPI) "UPI: ${request.upiId}" else "Bank: ${request.accountNumber}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GamingTextPrimary
                    )
                }
                StatusBadge(status = request.status.name)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(request.timestamp)),
                    fontSize = 11.sp,
                    color = GamingTextMuted
                )
                Text(
                    text = "₹${String.format("%.2f", request.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = GamingGold
                )
            }

            if (request.adminNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Admin Note: ${request.adminNote}",
                    fontSize = 11.sp,
                    color = GamingTextSecondary
                )
            }
        }
    }
}

@Composable
fun WithdrawalRequestDialog(
    availableBalance: Double,
    userName: String,
    userPhone: String,
    onDismiss: () -> Unit,
    onSubmit: (Double, WithdrawalMethod, String, String, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf("100") }
    var selectedMethod by remember { mutableStateOf(WithdrawalMethod.UPI) }
    var upiId by remember { mutableStateOf("") }
    var accNum by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf(userName) }
    var ifsc by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GamingCardSurface,
        title = {
            Text(
                text = "Request Withdrawal",
                fontWeight = FontWeight.Bold,
                color = GamingTextPrimary,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Available Balance: ₹${String.format("%.2f", availableBalance)}",
                    fontSize = 12.sp,
                    color = GamingGold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Amount Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("100", "200", "500", "1000").forEach { preset ->
                        FilterChip(
                            selected = amountText == preset,
                            onClick = { amountText = preset },
                            label = { Text("₹$preset", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GamingRed,
                                selectedLabelColor = Color.White,
                                containerColor = GamingCardSurfaceElevated,
                                labelColor = GamingTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (Min ₹100)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GamingRed,
                        unfocusedBorderColor = GamingCardBorder,
                        focusedTextColor = GamingTextPrimary,
                        unfocusedTextColor = GamingTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Method Tabs (UPI vs Bank)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedMethod = WithdrawalMethod.UPI },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMethod == WithdrawalMethod.UPI) GamingRed else GamingCardSurfaceElevated
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("UPI", fontSize = 12.sp, color = Color.White)
                    }

                    Button(
                        onClick = { selectedMethod = WithdrawalMethod.BANK_TRANSFER },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMethod == WithdrawalMethod.BANK_TRANSFER) GamingRed else GamingCardSurfaceElevated
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Bank Transfer", fontSize = 12.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedMethod == WithdrawalMethod.UPI) {
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("Enter UPI ID (e.g. mobile@upi)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GamingRed,
                            unfocusedBorderColor = GamingCardBorder,
                            focusedTextColor = GamingTextPrimary,
                            unfocusedTextColor = GamingTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("withdrawal_upi_input")
                    )
                } else {
                    OutlinedTextField(
                        value = holderName,
                        onValueChange = { holderName = it },
                        label = { Text("Account Holder Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GamingRed,
                            unfocusedBorderColor = GamingCardBorder,
                            focusedTextColor = GamingTextPrimary,
                            unfocusedTextColor = GamingTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = accNum,
                        onValueChange = { accNum = it },
                        label = { Text("Bank Account Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GamingRed,
                            unfocusedBorderColor = GamingCardBorder,
                            focusedTextColor = GamingTextPrimary,
                            unfocusedTextColor = GamingTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = ifsc,
                        onValueChange = { ifsc = it.uppercase() },
                        label = { Text("IFSC Code (e.g. SBIN0001234)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GamingRed,
                            unfocusedBorderColor = GamingCardBorder,
                            focusedTextColor = GamingTextPrimary,
                            unfocusedTextColor = GamingTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg!!, color = GamingRed, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount < 100.0) {
                        errorMsg = "Minimum withdrawal is ₹100"
                        return@Button
                    }
                    if (amount > availableBalance) {
                        errorMsg = "Insufficient balance!"
                        return@Button
                    }
                    if (selectedMethod == WithdrawalMethod.UPI && upiId.isBlank()) {
                        errorMsg = "Please enter valid UPI ID"
                        return@Button
                    }
                    if (selectedMethod == WithdrawalMethod.BANK_TRANSFER && (accNum.isBlank() || ifsc.isBlank())) {
                        errorMsg = "Please fill account number and IFSC"
                        return@Button
                    }
                    errorMsg = null
                    onSubmit(amount, selectedMethod, upiId, accNum, holderName, ifsc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                modifier = Modifier.testTag("submit_withdrawal_button")
            ) {
                Text("Confirm Payout", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GamingCardBorder)
            ) {
                Text("Cancel", color = GamingTextSecondary)
            }
        }
    )
}

@Composable
fun EmptyListState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GamingCardSurface)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = GamingTextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
