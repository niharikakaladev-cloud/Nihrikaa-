package com.example.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalMethod
import com.example.data.model.WithdrawalRequest
import com.example.data.model.WithdrawalStatus
import com.example.data.repository.EarningRepository
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
fun AdminDashboardScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val walletRepo = remember { WalletRepository.getInstance() }
    val earningRepo = remember { EarningRepository.getInstance() }

    // Enforce Authorization Check
    if (!userProfile.isAdmin) {
        Surface(color = GamingDarkBackground, modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = GamingRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Access Denied (403)",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You do not have administrative privileges to view the Niharika Games Control Room.",
                    fontSize = 13.sp,
                    color = GamingTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = GamingCardBorder)
                ) {
                    Text("Return to Safety", color = Color.White)
                }
            }
        }
        return
    }

    val allWithdrawals by walletRepo.allWithdrawalsForAdmin.collectAsState()
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Withdrawals, 1: Users, 2: Broadcast, 3: Settings
    var searchQuery by remember { mutableStateOf("") }

    // Broadcast form state
    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }

    Surface(
        color = GamingDarkBackground,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GamingTextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Control Room",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = GamingGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(status = "ROOT")
                    }
                    Text(
                        text = "Real-time moderation & financial approvals",
                        fontSize = 11.sp,
                        color = GamingTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                containerColor = GamingCardSurface,
                contentColor = GamingGold,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = GamingGold
                    )
                }
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text("Withdrawals (${allWithdrawals.count { it.status == WithdrawalStatus.PENDING }})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text("User Directory", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    text = { Text("Broadcast Alert", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 3,
                    onClick = { selectedSection = 3 },
                    text = { Text("Game Odds", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedSection) {
                0 -> {
                    // Payout / Withdrawal Management
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Pending Payout Requests",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamingTextPrimary
                            )
                        }

                        if (allWithdrawals.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GamingCardSurface)
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No payout requests found.", color = GamingTextMuted, fontSize = 13.sp)
                                }
                            }
                        } else {
                            items(allWithdrawals) { req ->
                                AdminWithdrawalCard(
                                    request = req,
                                    onApprove = {
                                        walletRepo.updateWithdrawalStatusByAdmin(
                                            requestId = req.id,
                                            newStatus = WithdrawalStatus.APPROVED,
                                            adminNote = "Approved & processed via IMPS/UPI reference #TXN${System.currentTimeMillis() % 100000}"
                                        )
                                        Toast.makeText(context, "Withdrawal Approved", Toast.LENGTH_SHORT).show()
                                    },
                                    onReject = {
                                        walletRepo.updateWithdrawalStatusByAdmin(
                                            requestId = req.id,
                                            newStatus = WithdrawalStatus.REJECTED,
                                            adminNote = "Rejected: Invalid payment details provided. Funds returned to balance."
                                        )
                                        Toast.makeText(context, "Withdrawal Rejected & Refunded", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // User Management & Search
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by user name, phone or UID...") },
                            singleLine = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GamingGold) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GamingGold,
                                unfocusedBorderColor = GamingCardBorder,
                                focusedTextColor = GamingTextPrimary,
                                unfocusedTextColor = GamingTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("admin_user_search_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                GamingCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(GamingCardSurfaceElevated),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Default.People, contentDescription = null, tint = GamingGold)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(text = userProfile.name, fontWeight = FontWeight.Bold, color = GamingTextPrimary, fontSize = 14.sp)
                                                    Text(text = "UID: ${userProfile.uid}", color = GamingTextMuted, fontSize = 11.sp)
                                                }
                                            }
                                            StatusBadge(status = if (userProfile.isAdmin) "ADMIN" else "PLAYER")
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Phone: ${userProfile.phone.ifBlank { "Google Auth" }}", fontSize = 12.sp, color = GamingTextSecondary)
                                            Text(text = "KYC: Verified", fontSize = 12.sp, color = GamingEmerald)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Broadcast Notifications
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(text = "Broadcast Global Announcement", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GamingTextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Push alerts to all active gaming players instantly", fontSize = 12.sp, color = GamingTextSecondary)

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = notifTitle,
                            onValueChange = { notifTitle = it },
                            label = { Text("Notification Title") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GamingGold,
                                unfocusedBorderColor = GamingCardBorder,
                                focusedTextColor = GamingTextPrimary,
                                unfocusedTextColor = GamingTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notifMessage,
                            onValueChange = { notifMessage = it },
                            label = { Text("Message Body") },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GamingGold,
                                unfocusedBorderColor = GamingCardBorder,
                                focusedTextColor = GamingTextPrimary,
                                unfocusedTextColor = GamingTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().height(110.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (notifTitle.isNotBlank() && notifMessage.isNotBlank()) {
                                    earningRepo.addBroadcastNotification(notifTitle, notifMessage, "ANNOUNCEMENT")
                                    notifTitle = ""
                                    notifMessage = ""
                                    Toast.makeText(context, "Announcement Broadcasted to All Gamers!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("admin_send_broadcast_button")
                        ) {
                            Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Global Broadcast", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                3 -> {
                    // Game & Spin Odds Settings
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(text = "Game Configuration & Fairness Controls", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GamingTextPrimary)
                        Spacer(modifier = Modifier.height(14.dp))

                        GamingCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(text = "Lucky Spin Parameters", fontWeight = FontWeight.Bold, color = GamingGold)
                                Text(text = "Daily limit per user: 5 Spins (Default)", fontSize = 12.sp, color = GamingTextSecondary)
                                Text(text = "Jackpot Multiplier: ₹500 (Max cap)", fontSize = 12.sp, color = GamingTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "RNG Engine: Cryptographically Secure Pseudo-Random Number Generator (CSPRNG)", fontSize = 11.sp, color = GamingEmerald)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        GamingCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(text = "Anti-Fraud & Client Tamper Protection", fontWeight = FontWeight.Bold, color = GamingRed)
                                Text(text = "• Client wallet edits are blocked by server-side rules.", fontSize = 12.sp, color = GamingTextSecondary)
                                Text(text = "• Withdrawals require minimum ₹100 account threshold.", fontSize = 12.sp, color = GamingTextSecondary)
                                Text(text = "• Duplicate account phone checks active.", fontSize = 12.sp, color = GamingTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalCard(
    request: WithdrawalRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    GamingCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "User: ${request.userName}",
                        fontWeight = FontWeight.Bold,
                        color = GamingTextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Phone: ${request.userPhone}",
                        fontSize = 11.sp,
                        color = GamingTextMuted
                    )
                }
                StatusBadge(status = request.status.name)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Amount: ₹${String.format("%.2f", request.amount)}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = GamingGold
            )

            Text(
                text = if (request.method == WithdrawalMethod.UPI) "Payout Destination: UPI [${request.upiId}]"
                else "Bank A/c: ${request.accountNumber} | IFSC: ${request.ifscCode} | Holder: ${request.accountHolder}",
                fontSize = 12.sp,
                color = GamingTextSecondary
            )

            if (request.status == WithdrawalStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = GamingCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("admin_reject_${request.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = GamingRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject & Refund", fontSize = 12.sp, color = GamingRed)
                    }

                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = GamingEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("admin_approve_${request.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve Payout", fontSize = 12.sp, color = Color.White)
                    }
                }
            } else if (request.adminNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Resolution: ${request.adminNote}",
                    fontSize = 11.sp,
                    color = GamingTextMuted
                )
            }
        }
    }
}
