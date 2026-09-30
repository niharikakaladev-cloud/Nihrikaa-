package com.example.ui.screens.games.teenpatti

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.GameRepository
import com.example.ui.components.GamingCard
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
import kotlin.random.Random

data class PlayingCard(
    val rank: String,
    val suit: String,
    val value: Int
)

data class TeenPattiRoundResult(
    val playerHand: String,
    val dealerHand: String,
    val won: Boolean,
    val pot: Int
)

@Composable
fun TeenPattiScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameRepo = remember { GameRepository.getInstance() }

    val suits = listOf("♠", "♥", "♦", "♣")
    val ranks = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")

    fun generateHand(): List<PlayingCard> {
        val picked = mutableListOf<PlayingCard>()
        while (picked.size < 3) {
            val rIdx = Random.nextInt(ranks.size)
            val sIdx = Random.nextInt(suits.size)
            val card = PlayingCard(ranks[rIdx], suits[sIdx], rIdx + 2)
            if (!picked.contains(card)) {
                picked.add(card)
            }
        }
        return picked
    }

    var playerCards by remember { mutableStateOf(generateHand()) }
    var dealerCards by remember { mutableStateOf(generateHand()) }
    var areCardsSeen by remember { mutableStateOf(false) }
    var demoPot by remember { mutableIntStateOf(100) }
    var freePlayChips by remember { mutableIntStateOf(1000) }
    var showDownResult by remember { mutableStateOf(false) }
    var isPlayerWinner by remember { mutableStateOf(false) }
    val roundHistory = remember { mutableStateListOf<TeenPattiRoundResult>() }

    fun evaluateHand(cards: List<PlayingCard>): String {
        val sorted = cards.sortedBy { it.value }
        val isFlush = cards.all { it.suit == cards[0].suit }
        val isTrail = cards[0].rank == cards[1].rank && cards[1].rank == cards[2].rank
        val isStraight = (sorted[0].value + 1 == sorted[1].value && sorted[1].value + 1 == sorted[2].value) ||
                (sorted[0].value == 2 && sorted[1].value == 3 && sorted[2].value == 14) // A-2-3

        return when {
            isTrail -> "Trail (Trio) - Set of ${sorted[0].rank}"
            isStraight && isFlush -> "Pure Sequence!"
            isStraight -> "Sequence (Straight)"
            isFlush -> "Color (Flush)"
            sorted[0].rank == sorted[1].rank || sorted[1].rank == sorted[2].rank -> "Pair"
            else -> "High Card: ${sorted[2].rank}"
        }
    }

    fun startNewRound() {
        playerCards = generateHand()
        dealerCards = generateHand()
        areCardsSeen = false
        demoPot = 100
        freePlayChips -= 50
        showDownResult = false
    }

    fun showDown() {
        val playerStrength = playerCards.sumOf { it.value }
        val dealerStrength = dealerCards.sumOf { it.value }
        val won = playerStrength >= dealerStrength
        isPlayerWinner = won
        showDownResult = true
        if (won) {
            freePlayChips += demoPot
        }
        roundHistory.add(
            0,
            TeenPattiRoundResult(
                playerHand = evaluateHand(playerCards),
                dealerHand = evaluateHand(dealerCards),
                won = won,
                pot = demoPot
            )
        )
        gameRepo.recordGameScore(userProfile.uid, "Teen Patti", if (won) "Player Won ($demoPot chips)" else "Dealer Won")
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("teenpatti_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GamingTextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Teen Patti Indian Poker", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GamingTextPrimary)
                    Text(text = "Demo Chips: $freePlayChips • Pot: $demoPot", fontSize = 12.sp, color = GamingGold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Casino Velvet Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF380812), Color(0xFF1F080F), Color(0xFF10070B))
                        )
                    )
                    .border(2.dp, GamingGold.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dealer Area
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SYSTEM DEALER", fontSize = 11.sp, color = GamingTextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            dealerCards.forEach { card ->
                                CardItem(
                                    card = card,
                                    isRevealed = showDownResult
                                )
                            }
                        }
                    }

                    // Pot Center Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(GamingGold.copy(alpha = 0.2f))
                            .border(1.dp, GamingGold, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "POT: $demoPot DEMO CHIPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = GamingGold
                        )
                    }

                    // Player Area
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "YOU (${userProfile.name})", fontSize = 12.sp, color = GamingTextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (areCardsSeen) evaluateHand(playerCards) else "Playing Blind",
                                fontSize = 11.sp,
                                color = if (areCardsSeen) GamingGold else GamingTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            playerCards.forEach { card ->
                                CardItem(
                                    card = card,
                                    isRevealed = areCardsSeen || showDownResult
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { areCardsSeen = true },
                    enabled = !areCardsSeen && !showDownResult,
                    colors = ButtonDefaults.buttonColors(containerColor = GamingCardSurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = GamingGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "See Cards", fontSize = 12.sp, color = GamingTextPrimary)
                }

                Button(
                    onClick = {
                        demoPot += 50
                        freePlayChips -= 50
                    },
                    enabled = !showDownResult,
                    colors = ButtonDefaults.buttonColors(containerColor = GamingGold.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Chaal +50", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showDown() },
                    enabled = !showDownResult,
                    colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("teenpatti_show_button")
                ) {
                    Text(text = "Showdown", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Round History
            Text(
                text = "Round History",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GamingTextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (roundHistory.isEmpty()) {
                    item {
                        Text(text = "No rounds played yet. Place demo chaal & tap Showdown!", fontSize = 12.sp, color = GamingTextMuted)
                    }
                } else {
                    items(roundHistory) { item ->
                        GamingCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (item.won) "Round Won (+${item.pot} Chips)" else "Round Lost",
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.won) GamingEmerald else GamingRed,
                                        fontSize = 13.sp
                                    )
                                    Text(text = "Your Hand: ${item.playerHand}", fontSize = 11.sp, color = GamingTextSecondary)
                                }
                                Text(
                                    text = if (item.won) "WIN" else "LOSS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.won) GamingEmerald else GamingRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Showdown Dialog
        if (showDownResult) {
            AlertDialog(
                onDismissRequest = { showDownResult = false },
                containerColor = GamingCardSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = GamingGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isPlayerWinner) "You Won The Pot!" else "Dealer Took The Pot", color = GamingTextPrimary)
                    }
                },
                text = {
                    Column {
                        Text(text = "Your Hand: ${evaluateHand(playerCards)}", color = GamingGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Dealer's Hand: ${evaluateHand(dealerCards)}", color = GamingTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPlayerWinner) "You claimed $demoPot free demo chips!" else "Better luck next hand!",
                            fontSize = 13.sp,
                            color = GamingTextPrimary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { startNewRound() },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed)
                    ) {
                        Text("Next Hand", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun CardItem(
    card: PlayingCard,
    isRevealed: Boolean
) {
    Box(
        modifier = Modifier
            .size(width = 54.dp, height = 76.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isRevealed) Color.White else Color(0xFF1E1428))
            .border(1.dp, if (isRevealed) Color(0xFFCCCCCC) else GamingRed, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isRevealed) {
            val isRedSuit = card.suit == "♥" || card.suit == "♦"
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = card.rank,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isRedSuit) Color(0xFFD32F2F) else Color(0xFF212121)
                )
                Text(
                    text = card.suit,
                    fontSize = 16.sp,
                    color = if (isRedSuit) Color(0xFFD32F2F) else Color(0xFF212121)
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "NG", fontSize = 12.sp, fontWeight = FontWeight.Black, color = GamingRed)
                Text(text = "🂠", fontSize = 18.sp, color = GamingGold)
            }
        }
    }
}
