const admin = require("firebase-admin");

const signUp = async (req, res) => {
  try {
    const { email, password, displayName } = req.body;
    const userRecord = await admin.auth().createUser({
      email,
      password,
      displayName,
    });
    res.status(201).json({ message: "User created successfully", userRecord });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const signIn = async (req, res) => {
  try {
    const { idToken } = req.body;
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    const uid = decodedToken.uid;
    const userRecord = await admin.auth().getUser(uid);

    res.status(200).json({
      message: "Token verified. User signed in.",
      uid: userRecord.uid,
      email: userRecord.email,
    });
  } catch (error) {
    res.status(401).json({ error: "Invalid or expired ID token" });
  }
};

module.exports = {
  signUp,
  signIn,
};