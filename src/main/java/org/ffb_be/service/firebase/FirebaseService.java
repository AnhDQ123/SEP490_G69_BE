package org.ffb_be.service.firebase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class FirebaseService {
    @Autowired
    private FirebaseMessaging firebaseMessaging;

    public String sendNotification(String token, String title, String body) throws Exception {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        String response = firebaseMessaging.send(message);
        return response; // Trả về message ID
    }
}
