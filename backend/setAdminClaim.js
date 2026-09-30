/**
 * Niharika Games - Set Admin Custom Claims
 * 
 * Usage:
 * 1. Place your serviceAccountKey.json in this directory (or update the path below).
 * 2. Run: node setAdminClaim.js
 */

const admin = require("firebase-admin");
const path = require("path");

// Load the Service Account Key
const serviceAccountPath = process.env.GOOGLE_APPLICATION_CREDENTIALS || path.join(__dirname, "serviceAccountKey.json");

let serviceAccount;
try {
  serviceAccount = require(serviceAccountPath);
} catch (err) {
  console.error("❌ Error loading serviceAccountKey.json!");
  console.error("Please place your downloaded serviceAccountKey.json in the backend/ folder.");
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const ADMIN_EMAIL = "niharikakaladev@gmail.com";

async function grantAdminRole(email) {
  try {
    const user = await admin.auth().getUserByEmail(email);
    console.log(`Found user: ${user.email} (UID: ${user.uid})`);

    // Set custom claims: { admin: true }
    await admin.auth().setCustomUserClaims(user.uid, { admin: true });
    console.log(`✅ Successfully granted admin claim { admin: true } to ${email}`);

    // Update Firestore user document if present
    const db = admin.firestore();
    await db.collection("users").doc(user.uid).set(
      {
        isAdmin: true,
        email: email,
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      },
      { merge: true }
    );
    console.log(`✅ Updated Firestore user record for ${user.uid}`);
    process.exit(0);
  } catch (error) {
    console.error("❌ Failed to set admin claim:", error.message);
    process.exit(1);
  }
}

grantAdminRole(ADMIN_EMAIL);
