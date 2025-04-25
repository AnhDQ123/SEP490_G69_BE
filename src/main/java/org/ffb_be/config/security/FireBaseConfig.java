package org.ffb_be.config.security;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FireBaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        // Tải thông tin xác thực từ tài khoản dịch vụ đã được xác thực bởi Google Cloud SDK
        InputStream credentialsStream = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");

        if (credentialsStream == null) {
            throw new IOException("Unable to find the service account key file.");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .build();

        FirebaseApp.initializeApp(options);

        return FirebaseMessaging.getInstance();
    }
}
