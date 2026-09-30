package com.example.ui.screens.earning

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EarningTask
import com.example.data.model.UserProfile
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

@Composable
fun EarningScreen(
    userProfile: UserProfile,
    onNavigateToSpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val earningRepo = remember { EarningRepository.getInstance() }
    val walletRepo = remember { WalletRepository.getInstance() }

    val tasks by earningRepo.tasks.collectAsState()
    val wallet by walletRepo.wallet.collectAsState()
    val referralsCount by earningRepo.referralsCount.collectAsState()
    val referralEarnings by earningRepo.referralEarnings.collectAsState()

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
                        text = "Rewards & Earning Center",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GamingTextPrimary
                    )
                    Text(
                        text = "Complete genuine tasks, spin the wheel and invite friends to earn",
                        fontSize = 12.sp,
                        color = GamingTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Dashboard Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(GamingCardSurfaceElevated)
                        .border(1.dp, GamingCardBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Earned", fontSize = 11.sp, color = GamingTextMuted)
                            Text(
                                text = "₹${String.format("%.2f", wallet.totalEarnings)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamingEmerald
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(GamingCardBorder))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Friends Invited", fontSize = 11.sp, color = GamingTextMuted)
                            Text(
                                text = "$referralsCount Gamers",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamingGold
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(GamingCardBorder))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Referral Bonus", fontSize = 11.sp, color = GamingTextMuted)
                            Text(
                                text = "₹${String.format("%.2f", referralEarnings)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
            }

            // Referral Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = GamingCardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GamingGold.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GamingGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GroupAdd,
                                        contentDescription = null,
                                        tint = GamingGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Refer & Earn ₹25",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GamingTextPrimary
                                    )
                                    Text(
                                        text = "Get ₹25 when your friend joins with your code",
                                        fontSize = 11.sp,
                                        color = GamingTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Code Box & Actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1F1C2B))
                                .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "YOUR REFERRAL CODE", fontSize = 9.sp, color = GamingTextMuted)
                                Text(
                                    text = userProfile.referralCode.ifBlank { "NG9921" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GamingGold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val code = userProfile.referralCode.ifBlank { "NG9921" }
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        cm?.setPrimaryClip(ClipData.newPlainText("Referral Code", code))
                                        Toast.makeText(context, "Copied: $code", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GamingCardBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("copy_referral_code_button")
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 11.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        val code = userProfile.referralCode.ifBlank { "NG9921" }
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Join me on Niharika Games! Play Ludo, Carrom, Teen Patti and Spin Wheel to win rewards. Use my referral code: $code"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Referral Code"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("share_referral_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Invite", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Daily Tasks Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Tasks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingTextPrimary
                    )
                    Text(
                        text = "Resets Daily at Midnight",
                        fontSize = 11.sp,
                        color = GamingTextMuted
                    )
                }
            }

            items(tasks) { task ->
                TaskItemCard(
                    task = task,
                    onClaim = {
                        earningRepo.claimTaskReward(
                            userId = userProfile.uid,
                            taskId = task.id,
                            onResult = { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }

            // Video / Ad Partner Transparency Note
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GamingCardSurfaceElevated)
                        .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GamingGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Reward Verification: All earned coins and bonuses are securely validated on the server. Real money rewards are only credited after verification.",
                            fontSize = 11.sp,
                            color = GamingTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: EarningTask,
    onClaim: () -> Unit
) {
    GamingCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.subtitle,
                        fontSize = 11.sp,
                        color = GamingTextSecondary
                    )
                }

                // Reward Badge
                Text(
                    text = "+₹${task.reward.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = GamingGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar and Claim Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            fontSize = 10.sp,
                            color = GamingTextMuted
                        )
                        Text(
                            text = "${task.progress}/${task.maxProgress}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GamingTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (task.progress.toFloat() / task.maxProgress).coerceIn(0f, 1f) },
                        color = GamingEmerald,
                        trackColor = GamingCardBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                Button(
                    onClick = onClaim,
                    enabled = !task.isCompleted && task.progress >= task.maxProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamingEmerald,
                        disabledContainerColor = if (task.isCompleted) GamingCardBorder else GamingRed.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("claim_task_${task.id}")
                ) {
                    Text(
                        text = if (task.isCompleted) "Claimed" else if (task.progress >= task.maxProgress) "Claim" else "In Progress",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) GamingTextMuted else Color.White
                    )
                }
            }
        }
    }
}
