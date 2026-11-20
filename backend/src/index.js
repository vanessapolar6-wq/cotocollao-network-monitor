const { createApp } = require("./app");

let firebaseAdmin = null;
try {
  firebaseAdmin = require("firebase-admin");
  if (firebaseAdmin.apps.length === 0) {
    firebaseAdmin.initializeApp();
  }
} catch {
  // firebase-admin optional during offline unit tests
}

const PORT = Number(process.env.PORT || 8080);
const COLLECTOR_API_TOKEN = process.env.COLLECTOR_API_TOKEN || "";

const verifyClientToken = firebaseAdmin
  ? async (idToken) => {
      try {
        await firebaseAdmin.auth().verifyIdToken(idToken);
        return true;
      } catch {
        return false;
      }
    }
  : null;

const { server } = createApp({
  collectorToken: COLLECTOR_API_TOKEN,
  verifyClientToken,
});

server.listen(PORT, () => {
  console.log(`Cotocollao NOC Backend listening on port ${PORT}`);
});
