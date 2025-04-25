package org.ffb_be.controller;
import lombok.RequiredArgsConstructor;
import org.ffb_be.service.firebase.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/api/noti")
public class FirebaseController {

    private final FirebaseService firebaseNotificationService;

    @PostMapping("/sendNotification")
    public String sendNotification(@RequestParam String token, @RequestParam String title, @RequestParam String body) {
        try {
            return firebaseNotificationService.sendNotification(token, title, body);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending notification";
        }
    }
}
