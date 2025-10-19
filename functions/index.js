const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// Cloud Function to send a notification when a new chat message is created.
exports.sendChatNotification = functions.firestore
    .document("chats/{chatId}/messages/{messageId}")
    .onCreate(async (snap, context) => {
      // Get the new message data
      const newMessage = snap.data();
      const senderId = newMessage.senderId;
      const receiverId = newMessage.receiverId;

      // Get the sender's user data to get their name
      const senderUserDoc = await admin
          .firestore()
          .collection("users")
          .doc(senderId)
          .get();
      const senderName = senderUserDoc.data().displayName || "Someone";

      // Get the recipient's user data to get their FCM token
      const receiverUserDoc = await admin
          .firestore()
          .collection("users")
          .doc(receiverId)
          .get();
      const receiverFcmToken = receiverUserDoc.data().fcmToken;

      if (receiverFcmToken) {
        // Construct the notification message
        const payload = {
          notification: {
            title: `New message from ${senderName}`,
            body: newMessage.text,
          },
          token: receiverFcmToken,
        };

        // Send the notification
        try {
          await admin.messaging().send(payload);
          console.log("Notification sent successfully");
        } catch (error) {
          console.log("Error sending notification:", error);
        }
      } else {
        console.log("Can not send notification, no FCM token for user");
      }
      return null;
    });
