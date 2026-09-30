package com.example.ui.screens.games.ludo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.GameRepository
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingCardSurface
import com.example.ui.theme.GamingCardSurfaceElevated
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingGold
import com.example.ui.theme.GamingRed
import com.example.ui.theme.GamingTextMuted
import com.example.ui.theme.GamingTextPrimary
import com.example.ui.theme.GamingTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class LudoPlayer(val title: String, val color: Color) {
    RED("Player (Red)", Color(0xFFE50914)),
    GREEN("Bot (Green)", Color(0xFF00C853)),
    YELLOW("Yellow", Color(0xFFFFD600)),
    BLUE("Blue", Color(0xFF2979FF))
}

data class LudoToken(
    val id: Int,
    val player: LudoPlayer,
    var position: Int = -1 // -1 is in base, 0..56 is along track, 57 is home
)

@Composable
fun LudoGameScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val gameRepo = remember { GameRepository.getInstance() }

    var diceValue by remember { mutableIntStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }
    var currentTurn by remember { mutableStateOf(LudoPlayer.RED) }
    var winner by remember { mutableStateOf<LudoPlayer?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    // Tokens: 2 tokens for Red, 2 tokens for Green (Bot) for fast mobile play
    val redTokens = remember {
        mutableStateListOf(
            LudoToken(1, LudoPlayer.RED, -1),
            LudoToken(2, LudoPlayer.RED, -1)
        )
    }
    val greenTokens = remember {
        mutableStateListOf(
            LudoToken(3, LudoPlayer.GREEN, -1),
            LudoToken(4, LudoPlayer.GREEN, -1)
        )
    }

    // Bot automatic turn trigger
    LaunchedEffect(currentTurn) {
        if (currentTurn == LudoPlayer.GREEN && winner == null) {
            delay(1000L)
            // Bot rolls dice
            diceValue = Random.nextInt(1, 7)
            delay(600L)

            // Bot moves token
            val movableToken = greenTokens.find { (it.position == -1 && diceValue == 6) || (it.position >= 0 && it.position + diceValue <= 57) }
            if (movableToken != null) {
                if (movableToken.position == -1 && diceValue == 6) {
                    movableToken.position = 0
                } else if (movableToken.position >= 0) {
                    movableToken.position += diceValue
                }
            }

            // Check if Green won
            if (greenTokens.all { it.position >= 57 }) {
                winner = LudoPlayer.GREEN
                showResultDialog = true
                gameRepo.recordGameScore(userProfile.uid, "Ludo Classic", "Bot Won")
            } else {
                currentTurn = LudoPlayer.RED
            }
        }
    }

    fun rollDiceForPlayer() {
        if (isRolling || currentTurn != LudoPlayer.RED || winner != null) return
        scope.launch {
            isRolling = true
            for (i in 1..6) {
                diceValue = Random.nextInt(1, 7)
                delay(80L)
            }
            isRolling = false

            // Check if player has moves
            val canMove = redTokens.any { (it.position == -1 && diceValue == 6) || (it.position >= 0 && it.position + diceValue <= 57) }
            if (!canMove) {
                delay(800L)
                currentTurn = LudoPlayer.GREEN
            }
        }
    }

    fun moveRedToken(token: LudoToken) {
        if (currentTurn != LudoPlayer.RED || isRolling || winner != null) return

        if (token.position == -1 && diceValue == 6) {
            token.position = 0
            // Extra turn on 6!
        } else if (token.position >= 0 && token.position + diceValue <= 57) {
            token.position += diceValue
            if (diceValue != 6) {
                currentTurn = LudoPlayer.GREEN
            }
        }

        // Check winner
        if (redTokens.all { it.position >= 57 }) {
            winner = LudoPlayer.RED
            showResultDialog = true
            gameRepo.recordGameScore(userProfile.uid, "Ludo Classic", "Player Won")
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("ludo_back_button")) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GamingTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ludo Classic Demo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingTextPrimary
                    )
                    Text(
                        text = if (currentTurn == LudoPlayer.RED) "Your Turn (Red)" else "Computer Thinking (Green)...",
                        fontSize = 12.sp,
                        color = currentTurn.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ludo Board Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1B1A24))
                    .border(3.dp, GamingCardBorder, RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 4 Home quadrants
                    // Top-Left (Red)
                    drawRect(
                        color = Color(0xFFD32F2F),
                        topLeft = Offset(0f, 0f),
                        size = Size(w * 0.4f, h * 0.4f)
                    )
                    drawRect(
                        color = Color(0xFF1B1A24),
                        topLeft = Offset(w * 0.08f, h * 0.08f),
                        size = Size(w * 0.24f, h * 0.24f)
                    )

                    // Top-Right (Green)
                    drawRect(
                        color = Color(0xFF388E3C),
                        topLeft = Offset(w * 0.6f, 0f),
                        size = Size(w * 0.4f, h * 0.4f)
                    )
                    drawRect(
                        color = Color(0xFF1B1A24),
                        topLeft = Offset(w * 0.68f, h * 0.08f),
                        size = Size(w * 0.24f, h * 0.24f)
                    )

                    // Bottom-Left (Blue)
                    drawRect(
                        color = Color(0xFF1976D2),
                        topLeft = Offset(0f, h * 0.6f),
                        size = Size(w * 0.4f, h * 0.4f)
                    )
                    drawRect(
                        color = Color(0xFF1B1A24),
                        topLeft = Offset(w * 0.08f, h * 0.68f),
                        size = Size(w * 0.24f, h * 0.24f)
                    )

                    // Bottom-Right (Yellow)
                    drawRect(
                        color = Color(0xFFFBC02D),
                        topLeft = Offset(w * 0.6f, h * 0.6f),
                        size = Size(w * 0.4f, h * 0.4f)
                    )
                    drawRect(
                        color = Color(0xFF1B1A24),
                        topLeft = Offset(w * 0.68f, h * 0.68f),
                        size = Size(w * 0.24f, h * 0.24f)
                    )

                    // Center Winning Box
                    drawRect(
                        color = Color(0xFF2E293F),
                        topLeft = Offset(w * 0.4f, h * 0.4f),
                        size = Size(w * 0.2f, h * 0.2f)
                    )

                    // Grid Lines
                    val stepW = w / 15f
                    val stepH = h / 15f
                    for (i in 0..15) {
                        drawLine(
                            color = Color(0x33FFFFFF),
                            start = Offset(i * stepW, 0f),
                            end = Offset(i * stepW, h),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color(0x33FFFFFF),
                            start = Offset(0f, i * stepH),
                            end = Offset(w, i * stepH),
                            strokeWidth = 1f
                        )
                    }
                }

                // Render Red Tokens overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                ) {
                    Text(text = "RED BASE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        redTokens.forEach { token ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (token.position >= 57) GamingGold else Color(0xFFFF1744))
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable { moveRedToken(token) }
                                    .testTag("red_token_${token.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (token.position == -1) "P" else "${token.position}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Render Green Tokens overlay (Bot)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "BOT BASE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        greenTokens.forEach { token ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (token.position >= 57) GamingGold else Color(0xFF00E676))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (token.position == -1) "B" else "${token.position}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Center Home Victory Area
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GamingGold.copy(alpha = 0.2f))
                        .border(1.dp, GamingGold, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Home Trophy",
                        tint = GamingGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dice & Interactive Controls Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GamingCardSurface)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Dice Visual Box
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2C283E))
                            .border(2.dp, GamingGold, RoundedCornerShape(14.dp))
                            .clickable { rollDiceForPlayer() }
                            .testTag("ludo_dice_roller"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$diceValue",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = GamingGold
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(
                            text = if (currentTurn == LudoPlayer.RED) "Tap Roll Dice!" else "Bot Playing...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GamingTextPrimary
                        )
                        Text(
                            text = if (diceValue == 6) "Rolled 6! Unlock pawn or roll again!" else "Tap token to advance on board",
                            fontSize = 11.sp,
                            color = GamingTextMuted
                        )
                    }

                    Button(
                        onClick = { rollDiceForPlayer() },
                        enabled = currentTurn == LudoPlayer.RED && !isRolling,
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("roll_dice_button")
                    ) {
                        Icon(imageVector = Icons.Default.Casino, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Roll", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Game Result Dialog
        if (showResultDialog) {
            AlertDialog(
                onDismissRequest = { showResultDialog = false },
                containerColor = GamingCardSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = GamingGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (winner == LudoPlayer.RED) "You Won!" else "Computer Won!", color = GamingTextPrimary)
                    }
                },
                text = {
                    Text(
                        text = if (winner == LudoPlayer.RED)
                            "Superb match! You navigated all your tokens into home base first."
                        else
                            "Great effort! Practice and challenge again to conquer the Ludo board.",
                        color = GamingTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResultDialog = false
                            redTokens.forEach { it.position = -1 }
                            greenTokens.forEach { it.position = -1 }
                            winner = null
                            currentTurn = LudoPlayer.RED
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed)
                    ) {
                        Text("Play Again", color = Color.White)
                    }
                }
            )
        }
    }
}
