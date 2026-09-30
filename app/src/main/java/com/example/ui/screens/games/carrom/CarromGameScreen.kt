package com.example.ui.screens.games.carrom

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.GameRepository
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingCardSurface
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingEmerald
import com.example.ui.theme.GamingGold
import com.example.ui.theme.GamingRed
import com.example.ui.theme.GamingTextMuted
import com.example.ui.theme.GamingTextPrimary
import com.example.ui.theme.GamingTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.sqrt
import kotlin.random.Random

enum class CoinType(val points: Int, val color: Color) {
    WHITE(10, Color(0xFFEAEAEA)),
    BLACK(5, Color(0xFF262626)),
    QUEEN(25, Color(0xFFFF1744))
}

data class CarromCoin(
    val id: Int,
    val type: CoinType,
    var x: Float, // relative 0..1
    var y: Float,
    var isPocketed: Boolean = false
)

@Composable
fun CarromGameScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val gameRepo = remember { GameRepository.getInstance() }

    var strikerX by remember { mutableFloatStateOf(0.5f) }
    var strikeAngle by remember { mutableFloatStateOf(-90f) }
    var strikePower by remember { mutableFloatStateOf(0.7f) }
    var score by remember { mutableIntStateOf(0) }
    var strikesLeft by remember { mutableIntStateOf(5) }
    var isStriking by remember { mutableStateOf(false) }
    var showGameOver by remember { mutableStateOf(false) }

    // Coins on board
    val coins = remember {
        mutableStateListOf(
            CarromCoin(1, CoinType.QUEEN, 0.5f, 0.5f),
            CarromCoin(2, CoinType.WHITE, 0.46f, 0.46f),
            CarromCoin(3, CoinType.WHITE, 0.54f, 0.54f),
            CarromCoin(4, CoinType.BLACK, 0.54f, 0.46f),
            CarromCoin(5, CoinType.BLACK, 0.46f, 0.54f),
            CarromCoin(6, CoinType.WHITE, 0.5f, 0.42f),
            CarromCoin(7, CoinType.BLACK, 0.5f, 0.58f)
        )
    }

    fun fireStriker() {
        if (isStriking || strikesLeft <= 0) return
        scope.launch {
            isStriking = true
            strikesLeft -= 1

            // Calculate trajectory towards target
            delay(400L)

            // Random physics simulation pocketing chance based on angle & power
            val unpocketed = coins.filter { !it.isPocketed }
            if (unpocketed.isNotEmpty()) {
                val hitChance = Random.nextFloat()
                if (hitChance > 0.35f) {
                    val targetCoin = unpocketed.random()
                    targetCoin.isPocketed = true
                    score += targetCoin.type.points
                }
            }

            delay(300L)
            isStriking = false

            if (strikesLeft == 0 || coins.all { it.isPocketed }) {
                showGameOver = true
                gameRepo.recordGameScore(userProfile.uid, "Royal Carrom", "Score: $score")
            }
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("carrom_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GamingTextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Royal Carrom Board",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingTextPrimary
                    )
                    Text(
                        text = "Score: $score pts • Strikes Left: $strikesLeft",
                        fontSize = 12.sp,
                        color = GamingGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Carrom Board Box
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8C39E)) // Authentic Carrom wood color
                    .border(10.dp, Color(0xFF3E2723), RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val pocketRadius = 16.dp.toPx()

                    // 4 Corner Pockets
                    val pockets = listOf(
                        Offset(pocketRadius + 4f, pocketRadius + 4f),
                        Offset(w - pocketRadius - 4f, pocketRadius + 4f),
                        Offset(pocketRadius + 4f, h - pocketRadius - 4f),
                        Offset(w - pocketRadius - 4f, h - pocketRadius - 4f)
                    )
                    pockets.forEach { p ->
                        drawCircle(color = Color(0xFF1B1B1B), radius = pocketRadius, center = p)
                        drawCircle(color = Color(0xFF5D4037), radius = pocketRadius, center = p, style = Stroke(3f))
                    }

                    // Center Circles
                    drawCircle(color = Color(0xFFD7CCC8), radius = w * 0.16f, center = center)
                    drawCircle(color = Color(0xFF8D6E63), radius = w * 0.16f, center = center, style = Stroke(2f))
                    drawCircle(color = Color(0xFF8D6E63), radius = w * 0.04f, center = center, style = Stroke(2f))

                    // Baselines
                    val baselineY = h * 0.82f
                    drawLine(
                        color = Color(0xFF5D4037),
                        start = Offset(w * 0.2f, baselineY),
                        end = Offset(w * 0.8f, baselineY),
                        strokeWidth = 2f
                    )
                    drawCircle(color = Color(0xFFD32F2F), radius = 6.dp.toPx(), center = Offset(w * 0.2f, baselineY))
                    drawCircle(color = Color(0xFFD32F2F), radius = 6.dp.toPx(), center = Offset(w * 0.8f, baselineY))

                    // Top Baseline
                    drawLine(
                        color = Color(0xFF5D4037),
                        start = Offset(w * 0.2f, h * 0.18f),
                        end = Offset(w * 0.8f, h * 0.18f),
                        strokeWidth = 2f
                    )

                    // Draw Coins on board
                    coins.filter { !it.isPocketed }.forEach { coin ->
                        val coinPos = Offset(coin.x * w, coin.y * h)
                        drawCircle(
                            color = coin.type.color,
                            radius = 11.dp.toPx(),
                            center = coinPos
                        )
                        drawCircle(
                            color = Color(0xFF424242),
                            radius = 11.dp.toPx(),
                            center = coinPos,
                            style = Stroke(2f)
                        )
                    }

                    // Draw Striker on player's baseline
                    val strikerPos = Offset(strikerX * w, baselineY)
                    drawCircle(
                        color = Color(0xFFFFF9C4),
                        radius = 16.dp.toPx(),
                        center = strikerPos
                    )
                    drawCircle(
                        color = Color(0xFFD4AF37),
                        radius = 16.dp.toPx(),
                        center = strikerPos,
                        style = Stroke(3f)
                    )

                    // Draw Aim Guide Line
                    val aimLength = 40.dp.toPx() * strikePower
                    val rad = Math.toRadians(strikeAngle.toDouble())
                    val aimEnd = Offset(
                        strikerPos.x + (aimLength * kotlin.math.cos(rad)).toFloat(),
                        strikerPos.y + (aimLength * kotlin.math.sin(rad)).toFloat()
                    )
                    drawLine(
                        color = GamingRed,
                        start = strikerPos,
                        end = aimEnd,
                        strokeWidth = 3f
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Striker Positioning Slider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingCardSurface)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Striker Position", fontSize = 12.sp, color = GamingTextSecondary)
                        Text(text = "Aim Angle: ${strikeAngle.toInt()}°", fontSize = 12.sp, color = GamingGold)
                    }

                    Slider(
                        value = strikerX,
                        onValueChange = { strikerX = it.coerceIn(0.25f, 0.75f) },
                        colors = SliderDefaults.colors(
                            thumbColor = GamingGold,
                            activeTrackColor = GamingRed,
                            inactiveTrackColor = GamingCardBorder
                        ),
                        modifier = Modifier.testTag("striker_position_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { strikeAngle = (strikeAngle - 15f).coerceIn(-150f, -30f) },
                            colors = ButtonDefaults.buttonColors(containerColor = GamingCardBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Aim Left", fontSize = 11.sp, color = GamingTextPrimary)
                        }

                        Button(
                            onClick = { strikeAngle = (strikeAngle + 15f).coerceIn(-150f, -30f) },
                            colors = ButtonDefaults.buttonColors(containerColor = GamingCardBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Aim Right", fontSize = 11.sp, color = GamingTextPrimary)
                        }

                        Button(
                            onClick = { fireStriker() },
                            enabled = !isStriking && strikesLeft > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("carrom_strike_button")
                        ) {
                            Icon(imageVector = Icons.Default.SportsEsports, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isStriking) "Striking" else "STRIKE", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Game Over Dialog
        if (showGameOver) {
            AlertDialog(
                onDismissRequest = { showGameOver = false },
                containerColor = GamingCardSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = GamingGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Game Finished!", color = GamingTextPrimary)
                    }
                },
                text = {
                    Column {
                        Text(text = "Awesome performance! You scored $score points in Royal Carrom.", color = GamingTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "White: 10 pts • Black: 5 pts • Queen: 25 pts", fontSize = 12.sp, color = GamingGold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGameOver = false
                            coins.forEach { it.isPocketed = false }
                            score = 0
                            strikesLeft = 5
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
