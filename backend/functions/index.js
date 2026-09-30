const functions = require("firebase-functions");
const admin = require("firebase-admin");

// When deployed to Firebase Cloud Functions, initializeApp() automatically uses default credentials
// When running locally with a service account, set GOOGLE_APPLICATION_CREDENTIALS
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

/**
 * 1. Automatically provision wallet upon user signup
 */
exports.onUserCreated = functions.auth.user().onCreate(async (user) => {
  const userId = user.uid;
  const isSuperAdmin = user.email === "niharikakaladev@gmail.com";

  const walletRef = db.collection("wallets").doc(userId);
  const userRef = db.collection("users").doc(userId);
  const txRef = db.collection("transactions").doc(`tx_welcome_${userId.substring(0, 6)}`);

  const batch = db.batch();

  // Create User Profile
  batch.set(
    userRef,
    {
      uid: userId,
      name: user.displayName || `Gamer ${userId.substring(0, 4)}`,
      email: user.email || "",
      phone: user.phoneNumber || "",
      photoUrl: user.photoURL || "",
      referralCode: `NG${userId.substring(userId.length - 4).toUpperCase()}`,
      isAdmin: isSuperAdmin,
      registrationDate: admin.firestore.FieldValue.serverTimestamp(),
      kycVerified: false,
      vipLevel: 1
    },
    { merge: true }
  );

  // Set custom user claims if super admin
  if (isSuperAdmin) {
    await admin.auth().setCustomUserClaims(userId, { admin: true });
  }

  // Initialize Wallet with ₹50 Welcome Bonus
  batch.set(walletRef, {
    userId: userId,
    balance: 50.0,
    totalEarnings: 50.0,
    pendingWithdrawal: 0.0,
    lastUpdated: admin.firestore.FieldValue.serverTimestamp()
  });

  // Welcome Bonus Transaction Record
  batch.set(txRef, {
    id: txRef.id,
    userId: userId,
    title: "Welcome Signup Bonus",
    amount: 50.0,
    type: "WELCOME_BONUS",
    status: "COMPLETED",
    timestamp: Date.now()
  });

  await batch.commit();
  console.log(`Wallet initialized for user: ${userId}`);
});

/**
 * 2. Secure Server-Side Wallet Credit (Task Rewards / Spin Rewards)
 */
exports.creditReward = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in to claim rewards.");
  }

  const userId = context.auth.uid;
  const { amount, title, type } = data;

  if (!amount || amount <= 0 || amount > 500) {
    throw new functions.https.HttpsError("invalid-argument", "Invalid reward amount.");
  }

  const walletRef = db.collection("wallets").doc(userId);
  const txRef = db.collection("transactions").doc();

  return db.runTransaction(async (transaction) => {
    const walletDoc = await transaction.get(walletRef);
    if (!walletDoc.exists) {
      throw new functions.https.HttpsError("not-found", "Wallet not found.");
    }

    const currentBalance = walletDoc.data().balance || 0;
    const currentEarnings = walletDoc.data().totalEarnings || 0;

    const newBalance = currentBalance + amount;
    const newEarnings = currentEarnings + amount;

    transaction.update(walletRef, {
      balance: newBalance,
      totalEarnings: newEarnings,
      lastUpdated: admin.firestore.FieldValue.serverTimestamp()
    });

    transaction.set(txRef, {
      id: txRef.id,
      userId: userId,
      title: title || "Gamer Reward",
      amount: amount,
      type: type || "TASK_REWARD",
      status: "COMPLETED",
      timestamp: Date.now()
    });

    return { success: true, balance: newBalance, earnings: newEarnings };
  });
});

/**
 * 3. Secure Server-Side Withdrawal Request
 */
exports.requestWithdrawal = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be logged in to withdraw.");
  }

  const userId = context.auth.uid;
  const { amount, method, upiId, accountNumber, accountHolder, ifscCode } = data;

  if (!amount || amount < 100) {
    throw new functions.https.HttpsError("invalid-argument", "Minimum withdrawal is ₹100.00.");
  }

  const walletRef = db.collection("wallets").doc(userId);
  const withdrawalRef = db.collection("withdrawals").doc();
  const txRef = db.collection("transactions").doc();

  return db.runTransaction(async (transaction) => {
    const walletDoc = await transaction.get(walletRef);
    if (!walletDoc.exists) {
      throw new functions.https.HttpsError("not-found", "Wallet not found.");
    }

    const currentBalance = walletDoc.data().balance || 0;
    const currentPending = walletDoc.data().pendingWithdrawal || 0;

    if (currentBalance < amount) {
      throw new functions.https.HttpsError("failed-precondition", "Insufficient withdrawable balance.");
    }

    const newBalance = currentBalance - amount;
    const newPending = currentPending + amount;

    transaction.update(walletRef, {
      balance: newBalance,
      pendingWithdrawal: newPending,
      lastUpdated: admin.firestore.FieldValue.serverTimestamp()
    });

    const withdrawalData = {
      id: withdrawalRef.id,
      userId: userId,
      userName: context.auth.token.name || "Gamer",
      userPhone: context.auth.token.phone_number || "",
      amount: amount,
      method: method || "UPI",
      upiId: upiId || "",
      accountNumber: accountNumber || "",
      accountHolder: accountHolder || "",
      ifscCode: ifscCode || "",
      status: "PENDING",
      timestamp: Date.now(),
      adminNote: ""
    };

    transaction.set(withdrawalRef, withdrawalData);

    transaction.set(txRef, {
      id: txRef.id,
      userId: userId,
      title: `Withdrawal Request (${method === "UPI" ? upiId : accountNumber})`,
      amount: amount,
      type: "WITHDRAWAL_REQUEST",
      status: "PENDING",
      timestamp: Date.now()
    });

    return { success: true, requestId: withdrawalRef.id, newBalance: newBalance };
  });
});

/**
 * 4. Admin Only: Process Withdrawal (Approve or Reject & Refund)
 */
exports.processWithdrawal = functions.https.onCall(async (data, context) => {
  const isAdmin = context.auth && (
    context.auth.token.admin === true ||
    context.auth.token.email === "niharikakaladev@gmail.com"
  );

  if (!isAdmin) {
    throw new functions.https.HttpsError("permission-denied", "Admin privileges required.");
  }

  const { requestId, status, adminNote } = data; // status: "APPROVED" or "REJECTED"
  const withdrawalRef = db.collection("withdrawals").doc(requestId);

  return db.runTransaction(async (transaction) => {
    const reqDoc = await transaction.get(withdrawalRef);
    if (!reqDoc.exists) {
      throw new functions.https.HttpsError("not-found", "Withdrawal request not found.");
    }

    const reqData = reqDoc.data();
    if (reqData.status !== "PENDING") {
      throw new functions.https.HttpsError("failed-precondition", "Withdrawal is already resolved.");
    }

    const userId = reqData.userId;
    const amount = reqData.amount;
    const walletRef = db.collection("wallets").doc(userId);
    const walletDoc = await transaction.get(walletRef);

    if (walletDoc.exists) {
      const currentBalance = walletDoc.data().balance || 0;
      const currentPending = walletDoc.data().pendingWithdrawal || 0;
      const newPending = Math.max(0, currentPending - amount);

      if (status === "REJECTED") {
        // Refund funds back to balance on rejection
        transaction.update(walletRef, {
          balance: currentBalance + amount,
          pendingWithdrawal: newPending,
          lastUpdated: admin.firestore.FieldValue.serverTimestamp()
        });
      } else {
        // Deduct pending balance on approval
        transaction.update(walletRef, {
          pendingWithdrawal: newPending,
          lastUpdated: admin.firestore.FieldValue.serverTimestamp()
        });
      }
    }

    transaction.update(withdrawalRef, {
      status: status,
      adminNote: adminNote || (status === "APPROVED" ? "Approved by Admin" : "Rejected by Admin"),
      resolvedAt: Date.now()
    });

    return { success: true, status: status };
  });
});

/**
 * 5. Admin Only: Broadcast Global Announcement
 */
exports.broadcastNotification = functions.https.onCall(async (data, context) => {
  const isAdmin = context.auth && (
    context.auth.token.admin === true ||
    context.auth.token.email === "niharikakaladev@gmail.com"
  );

  if (!isAdmin) {
    throw new functions.https.HttpsError("permission-denied", "Admin privileges required.");
  }

  const { title, message, type } = data;
  const notifRef = db.collection("notifications").doc();

  await notifRef.set({
    id: notifRef.id,
    title: title,
    message: message,
    type: type || "ANNOUNCEMENT",
    timestamp: Date.now()
  });

  return { success: true, id: notifRef.id };
});
