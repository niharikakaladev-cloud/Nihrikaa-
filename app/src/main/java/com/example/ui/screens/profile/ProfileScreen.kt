package com.example.ui.screens.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
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
fun ProfileScreen(
    userProfile: UserProfile,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authRepo = remember { AuthRepository.getInstance() }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Surface(
        color = GamingDarkBackground,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Title
                Text(
                    text = "Player Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = GamingTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Avatar & Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GamingCardSurface)
                        .border(1.dp, GamingCardBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B1D3A))
                                    .border(2.dp, GamingGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile.name.take(1).uppercase(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GamingGold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userProfile.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GamingTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { showEditProfileDialog = true },
                                        modifier = Modifier.size(24.dp).testTag("edit_profile_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Profile",
                                            tint = GamingGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (userProfile.phone.isNotBlank()) {
                                    Text(
                                        text = userProfile.phone,
                                        fontSize = 12.sp,
                                        color = GamingTextSecondary
                                    )
                                }
                                if (userProfile.email.isNotBlank()) {
                                    Text(
                                        text = userProfile.email,
                                        fontSize = 12.sp,
                                        color = GamingTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Member since
                                Text(
                                    text = "Member since ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(userProfile.registrationDate))}",
                                    fontSize = 11.sp,
                                    color = GamingTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // User ID Pill with Copy
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GamingCardSurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "UID: ${userProfile.uid}",
                                fontSize = 11.sp,
                                color = GamingTextMuted
                            )
                            Row(
                                modifier = Modifier.clickable {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cm?.setPrimaryClip(ClipData.newPlainText("User ID", userProfile.uid))
                                    Toast.makeText(context, "Copied UID", Toast.LENGTH_SHORT).show()
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = GamingGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Copy", fontSize = 11.sp, color = GamingGold)
                            }
                        }
                    }
                }
            }

            // Admin Control Room Button (MANDATORY: VISIBLE ONLY TO AUTHORIZED ADMIN)
            if (userProfile.isAdmin) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0C16)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, GamingRed, RoundedCornerShape(16.dp))
                            .clickable(onClick = onNavigateToAdmin)
                            .testTag("admin_portal_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(GamingRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Admin Control Room",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StatusBadge(status = "AUTHORIZED")
                                }
                                Text(
                                    text = "Manage withdrawals, users, games & broadcasts",
                                    fontSize = 11.sp,
                                    color = GamingTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Menu Items List
            item {
                Text(
                    text = "Preferences & Support",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingTextPrimary
                )
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Announcements & Notifications",
                    subtitle = "Payout alerts, rewards & maintenance notices",
                    onClick = onNavigateToNotifications,
                    tag = "profile_notifications"
                )
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.Security,
                    title = "Security & Fair Play",
                    subtitle = "RNG Certification, 128-bit encryption",
                    onClick = {
                        Toast.makeText(context, "Account protected with multi-factor authentication.", Toast.LENGTH_SHORT).show()
                    },
                    tag = "profile_security"
                )
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Help & Customer Support",
                    subtitle = "FAQs and 24x7 player assistance",
                    onClick = { showHelpDialog = true },
                    tag = "profile_help"
                )
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.Policy,
                    title = "Terms of Service",
                    subtitle = "Free-play rules, community standards",
                    onClick = { showTermsDialog = true },
                    tag = "profile_terms"
                )
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "Privacy Policy",
                    subtitle = "Data protection & confidentiality",
                    onClick = { showPrivacyDialog = true },
                    tag = "profile_privacy"
                )
            }

            // Logout Option
            item {
                ProfileMenuItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Logout Account",
                    subtitle = "Sign out from current device",
                    iconColor = GamingRed,
                    textColor = GamingRed,
                    onClick = { showLogoutConfirm = true },
                    tag = "profile_logout"
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            var newName by remember { mutableStateOf(userProfile.name) }
            var newEmail by remember { mutableStateOf(userProfile.email) }

            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                containerColor = GamingCardSurface,
                title = { Text("Edit Gamer Profile", color = GamingTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Gamer Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GamingRed,
                                unfocusedBorderColor = GamingCardBorder,
                                focusedTextColor = GamingTextPrimary,
                                unfocusedTextColor = GamingTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email Address") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GamingRed,
                                unfocusedBorderColor = GamingCardBorder,
                                focusedTextColor = GamingTextPrimary,
                                unfocusedTextColor = GamingTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authRepo.updateProfile(newName, newEmail)
                            showEditProfileDialog = false
                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showEditProfileDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingCardBorder)
                    ) {
                        Text("Cancel", color = GamingTextSecondary)
                    }
                }
            )
        }

        // Help Dialog
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                containerColor = GamingCardSurface,
                title = { Text("Niharika Games Support", color = GamingTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Need assistance with gameplay or payouts?", color = GamingTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Email: support@niharikagames.com", color = GamingGold, fontWeight = FontWeight.Bold)
                        Text(text = "Hours: 24x7 Customer Support", color = GamingTextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Withdrawals are verified and processed directly to your linked UPI ID or Bank account.", color = GamingTextSecondary, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showHelpDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed)
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            )
        }

        // Terms Dialog
        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = GamingCardSurface,
                title = { Text("Terms of Service", color = GamingTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "1. Niharika Games is designed for entertainment and skill development.\n2. Games are operated with fair play and random number generation standards.\n3. Demo points and rewards have no real financial debt obligation outside configured legitimate backend payout gateways.\n4. Users must be 18+ to register.",
                        color = GamingTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showTermsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed)
                    ) {
                        Text("Accept", color = Color.White)
                    }
                }
            )
        }

        // Privacy Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                containerColor = GamingCardSurface,
                title = { Text("Privacy Policy", color = GamingTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "We value your privacy. We collect phone numbers and email solely for authentication and secure payout validation. Your information is never sold to third parties.",
                        color = GamingTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showPrivacyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed)
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            )
        }

        // Logout Confirm Dialog
        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                containerColor = GamingCardSurface,
                title = { Text("Confirm Logout", color = GamingTextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to sign out from Niharika Games?", color = GamingTextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirm = false
                            authRepo.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingRed),
                        modifier = Modifier.testTag("confirm_logout_button")
                    ) {
                        Text("Logout", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showLogoutConfirm = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingCardBorder)
                    ) {
                        Text("Cancel", color = GamingTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color = GamingGold,
    textColor: Color = GamingTextPrimary,
    onClick: () -> Unit,
    tag: String
) {
    GamingCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GamingCardSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = GamingTextMuted
                )
            }
        }
    }
}
