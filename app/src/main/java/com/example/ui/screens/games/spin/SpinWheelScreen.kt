package com.example.ui.screens.games.spin

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.GamingGoldGradient
import com.example.ui.theme.GamingRed
import com.example.ui.theme.GamingRedDark
import com.example.ui.theme.GamingTextMuted
import com.example.ui.theme.GamingTextPrimary
import com.example.ui.theme.GamingTextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class SpinSector(
    val label: String,
    val amount: Double,
    val color: Color
)

@Composable
fun SpinWheelScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gameRepo = remember { GameRepository.getInstance() }
    val remainingSpins by gameRepo.remainingSpins.collectAsState()
    val spinHistory by gameRepo.spinHistory.collectAsState()

    val sectors = remember {
        listOf(
            SpinSector("₹50", 50.0, Color(0xFFC00A23)),
            SpinSector("₹10", 10.0, Color(0xFF1F1A2C)),
            SpinSector("₹100", 100.0, Color(0xFFE5A000)),
            SpinSector("Try Again", 0.0, Color(0xFF2C253B)),
            SpinSector("₹25", 25.0, Color(0xFF9D1B36)),
            SpinSector("₹200", 200.0, Color(0xFF1A1A24)),
            SpinSector("₹5", 5.0, Color(0xFF00897B)),
            SpinSector("₹500", 500.0, Color(0xFFD4AF37))
        )
    }

    var currentRotation by remember { mutableFloatStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var winningSector by remember { mutableStateOf<SpinSector?>(null) }

    val animatedRotation by animateFloatAsState(
        targetValue = currentRotation,
        animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
        finishedListener = {
            if (isSpinning) {
                isSpinning = false
                showResultDialog = true
                winningSector?.let { sector ->
                    gameRepo.recordSpinResult(
                        userId = userProfile.uid,
                        amount = sector.amount,
                        label = sector.label,
                        onSuccess = {
                            // Haptic vibration
                            try {
                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(150)
                                }
                            } catch (_: Exception) {}
                        }
                    )
                }
            }
        },
        label = "WheelRotation"
    )

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
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("spin_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GamingTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lucky Wheel Spin",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingTextPrimary
                    )
                    Text(
                        text = "Daily Free Spins: $remainingSpins/5 left",
                        fontSize = 12.sp,
                        color = GamingGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wheel Canvas Box
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Glow Ring
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .clip(CircleShape)
                        .border(6.dp, GamingGold.copy(alpha = 0.8f), CircleShape)
                )

                // Wheel Disc
                Canvas(
                    modifier = Modifier
                        .size(250.dp)
                        .rotate(animatedRotation)
                ) {
                    val sweepAngle = 360f / sectors.size
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    sectors.forEachIndexed { i, sector ->
                        val startAngle = i * sweepAngle - 90f
                        drawArc(
                            color = sector.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(radius * 2, radius * 2),
                            topLeft = Offset(center.x - radius, center.y - radius)
                        )

                        // Border between slices
                        drawArc(
                            color = GamingCardBorder,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            style = Stroke(width = 2f),
                            size = Size(radius * 2, radius * 2),
                            topLeft = Offset(center.x - radius, center.y - radius)
                        )

                        // Draw text
                        val angleInRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                        val textRadius = radius * 0.65f
                        val x = center.x + (textRadius * cos(angleInRad)).toFloat()
                        val y = center.y + (textRadius * sin(angleInRad)).toFloat()

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 32f
                                isFakeBoldText = true
                            }
                            drawText(sector.label, x, y + 10f, paint)
                        }
                    }

                    // Center Hub Rim
                    drawCircle(
                        color = Color(0xFF14131D),
                        radius = 28.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = GamingGold,
                        radius = 24.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = GamingRed,
                        radius = 16.dp.toPx(),
                        center = center
                    )
                }

                // Pointer Needle at Top
                Canvas(
                    modifier = Modifier
                        .size(34.dp)
                        .align(Alignment.TopCenter)
                ) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, size.height)
                        lineTo(0f, 0f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    drawPath(path, color = GamingGold)
                    drawPath(path, color = Color.White, style = Stroke(width = 2f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Spin CTA Button
            Button(
                onClick = {
                    if (!isSpinning && remainingSpins > 0) {
                        isSpinning = true
                        val selectedIdx = Random.nextInt(sectors.size)
                        winningSector = sectors[selectedIdx]

                        // Sector angle is 360 / 8 = 45 degrees
                        val sweep = 360f / sectors.size
                        // To land on selected index, pointer is at top (270 deg / -90 deg)
                        val targetSectorAngle = selectedIdx * sweep + sweep / 2f
                        val fullRotations = 360f * (5 + Random.nextInt(3))
                        currentRotation += fullRotations + (360f - (currentRotation % 360f)) + (360f - targetSectorAngle)
                    }
                },
                enabled = !isSpinning && remainingSpins > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamingRed,
                    disabledContainerColor = GamingCardBorder
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(52.dp)
                    .testTag("spin_wheel_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSpinning) "Spinning..." else if (remainingSpins > 0) "SPIN TO WIN" else "Daily Limit Reached",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Spin History Section
            Text(
                text = "Recent Spin History",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GamingTextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (spinHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No spins recorded yet. Spin today for rewards!",
                                fontSize = 12.sp,
                                color = GamingTextMuted
                            )
                        }
                    }
                } else {
                    items(spinHistory) { record ->
                        GamingCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (record.rewardAmount > 0) GamingEmerald else GamingTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Won: ${record.rewardLabel}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GamingTextPrimary
                                    )
                                }
                                Text(
                                    text = if (record.rewardAmount > 0) "+₹${record.rewardAmount}" else "₹0",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.rewardAmount > 0) GamingGold else GamingTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Result Dialog
        if (showResultDialog && winningSector != null) {
            AlertDialog(
                onDismissRequest = { showResultDialog = false },
                containerColor = GamingCardSurface,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GamingGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (winningSector!!.amount > 0) "Congratulations!" else "Better Luck!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GamingTextPrimary
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (winningSector!!.amount > 0)
                                "You won ${winningSector!!.label} credited directly to your wallet!"
                            else
                                "Don't worry! Try your luck again tomorrow.",
                            fontSize = 14.sp,
                            color = GamingTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = winningSector!!.label,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (winningSector!!.amount > 0) GamingGold else GamingRed
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showResultDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("claim_spin_reward_button")
                    ) {
                        Text("Claim & Continue", color = Color.White)
                    }
                }
            )
        }
    }
}
