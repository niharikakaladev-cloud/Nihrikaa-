package com.example.ui.screens.games.dragontiger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.GameRepository
import com.example.ui.screens.games.teenpatti.PlayingCard
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class BetChoice {
    DRAGON,
    TIE,
    TIGER
}

data class DTRound(
    val winner: BetChoice,
    val dragonCard: PlayingCard,
    val tigerCard: PlayingCard
)

@Composable
fun DragonTigerScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val gameRepo = remember { GameRepository.getInstance() }

    val suits = listOf("♠", "♥", "♦", "♣")
    val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")

    fun drawRandomCard(): PlayingCard {
        val rIdx = Random.nextInt(ranks.size)
        val sIdx = Random.nextInt(suits.size)
        return PlayingCard(ranks[rIdx], suits[sIdx], rIdx + 1)
    }

    var freeChips by remember { mutableIntStateOf(1000) }
    var selectedBet by remember { mutableStateOf<BetChoice?>(BetChoice.DRAGON) }
    var betAmount by remember { mutableIntStateOf(50) }
    var dragonCard by remember { mutableStateOf<PlayingCard?>(null) }
    var tigerCard by remember { mutableStateOf<PlayingCard?>(null) }
    var isDealing by remember { mutableStateOf(false) }
    var resultWinner by remember { mutableStateOf<BetChoice?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    val roadmapHistory = remember {
        mutableStateListOf(
            DTRound(BetChoice.DRAGON, PlayingCard("K", "♠", 13), PlayingCard("8", "♥", 8)),
            DTRound(BetChoice.TIGER, PlayingCard("4", "♦", 4), PlayingCard("Q", "♣", 12)),
            DTRound(BetChoice.DRAGON, PlayingCard("J", "♠", 11), PlayingCard("2", "♥", 2)),
            DTRound(BetChoice.TIE, PlayingCard("9", "♦", 9), PlayingCard("9", "♣", 9)),
            DTRound(BetChoice.TIGER, PlayingCard("3", "♠", 3), PlayingCard("7", "♥", 7))
        )
    }

    fun playRound() {
        if (isDealing || selectedBet == null || freeChips < betAmount) return
        scope.launch {
            isDealing = true
            freeChips -= betAmount
            dragonCard = null
            tigerCard = null
            resultWinner = null

            delay(600L)
            val dCard = drawRandomCard()
            dragonCard = dCard

            delay(600L)
            val tCard = drawRandomCard()
            tigerCard = tCard

            delay(500L)
            val winner = when {
                dCard.value > tCard.value -> BetChoice.DRAGON
                tCard.value > dCard.value -> BetChoice.TIGER
                else -> BetChoice.TIE
            }
            resultWinner = winner
            roadmapHistory.add(0, DTRound(winner, dCard, tCard))

            val won = selectedBet == winner
            if (won) {
                val multiplier = if (winner == BetChoice.TIE) 8 else 2
                freeChips += betAmount * multiplier
            }
            isDealing = false
            showResultDialog = true

            gameRepo.recordGameScore(userProfile.uid, "Dragon Tiger", "Outcome: ${winner.name}")
        }
    }

    Surface(
        color = GamingDarkBackground,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("dragontiger_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GamingTextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Dragon Tiger Arena", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GamingTextPrimary)
                    Text(text = "Free Play Chips: $freeChips", fontSize = 12.sp, color = GamingGold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dragon vs Tiger Arena Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF13111C))
                    .border(2.dp, GamingCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dragon Box
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "DRAGON", fontSize = 16.sp, fontWeight = FontWeight.Black, color = GamingRed)
                        Spacer(modifier = Modifier.height(10.dp))
                        DTCardDisplay(card = dragonCard, defaultLabel = "DRAGON")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (dragonCard != null) "Rank: ${dragonCard!!.value}" else "Waiting...",
                            fontSize = 11.sp,
                            color = GamingTextSecondary
                        )
                    }

                    // VS Emblem
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GamingGold.copy(alpha = 0.2f))
                                .border(1.dp, GamingGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "VS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = GamingGold)
                        }
                    }

                    // Tiger Box
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "TIGER", fontSize = 16.sp, fontWeight = FontWeight.Black, color = GamingGold)
                        Spacer(modifier = Modifier.height(10.dp))
                        DTCardDisplay(card = tigerCard, defaultLabel = "TIGER")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (tigerCard != null) "Rank: ${tigerCard!!.value}" else "Waiting...",
                            fontSize = 11.sp,
                            color = GamingTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Betting Options (Dragon 1:1, Tie 8:1, Tiger 1:1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BetTile(
                    title = "DRAGON",
                    payout = "1 : 1",
                    color = GamingRed,
                    isSelected = selectedBet == BetChoice.DRAGON,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedBet = BetChoice.DRAGON }
                )
                BetTile(
                    title = "TIE",
                    payout = "8 : 1",
                    color = GamingEmerald,
                    isSelected = selectedBet == BetChoice.TIE,
                    modifier = Modifier.weight(0.9f),
                    onClick = { selectedBet = BetChoice.TIE }
                )
                BetTile(
                    title = "TIGER",
                    payout = "1 : 1",
                    color = GamingGold,
                    isSelected = selectedBet == BetChoice.TIGER,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedBet = BetChoice.TIGER }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deal CTA Button
            Button(
                onClick = { playRound() },
                enabled = !isDealing && selectedBet != null && freeChips >= betAmount,
                colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dt_deal_button")
            ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDealing) "Dealing Cards..." else "DEAL ($betAmount Demo Chips)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Roadmap Beads History
            Text(
                text = "Roadmap History",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GamingTextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roadmapHistory) { round ->
                    val (badgeText, badgeColor) = when (round.winner) {
                        BetChoice.DRAGON -> "D" to GamingRed
                        BetChoice.TIGER -> "T" to GamingGold
                        BetChoice.TIE -> "Tie" to GamingEmerald
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                    }
                }
            }
        }

        // Result Dialog
        if (showResultDialog && resultWinner != null) {
            val won = selectedBet == resultWinner
            AlertDialog(
                onDismissRequest = { showResultDialog = false },
                containerColor = GamingCardSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = GamingGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (won) "You Won!" else "Round Ended", color = GamingTextPrimary)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "${resultWinner!!.name} Wins! (Dragon: ${dragonCard?.value ?: 0} vs Tiger: ${tigerCard?.value ?: 0})",
                            fontWeight = FontWeight.Bold,
                            color = GamingGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (won) "You picked ${selectedBet!!.name} and won free demo chips!" else "You picked ${selectedBet!!.name}. Better luck next round!",
                            color = GamingTextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showResultDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed)
                    ) {
                        Text("Next Round", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun DTCardDisplay(
    card: PlayingCard?,
    defaultLabel: String
) {
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 90.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (card != null) Color.White else Color(0xFF1E1A2C))
            .border(2.dp, if (card != null) Color(0xFFD4AF37) else GamingCardBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (card != null) {
            val isRed = card.suit == "♥" || card.suit == "♦"
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = card.rank, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isRed) Color(0xFFD32F2F) else Color(0xFF212121))
                Text(text = card.suit, fontSize = 20.sp, color = if (isRed) Color(0xFFD32F2F) else Color(0xFF212121))
            }
        } else {
            Text(text = defaultLabel.take(1), fontSize = 26.sp, fontWeight = FontWeight.Black, color = GamingTextMuted)
        }
    }
}

@Composable
fun BetTile(
    title: String,
    payout: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else GamingCardSurfaceElevated)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) color else GamingCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = payout, fontSize = 10.sp, color = GamingTextSecondary)
        }
    }
}
