package com.solicare.app.backend.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Configuration
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FirebaseConfig {

  @Value("${firebase.credentials.path:}")
  private String credentialsPath;

  @Value("${firebase.credentials.base64:}")
  private String credentialsBase64;

  @Bean
  public FirebaseMessaging firebaseMessaging() throws IOException {
    InputStream serviceAccount = getCredentialsInputStream();

    try (serviceAccount) {
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();
      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options);
      }
      return FirebaseMessaging.getInstance();
    }
  }

  private InputStream getCredentialsInputStream() throws IOException {
    // 1. Base64 환경변수가 있으면 사용
    if (credentialsBase64 != null && !credentialsBase64.isEmpty()) {
      log.info("Loading Firebase credentials from base64 environment variable");
      byte[] decoded = Base64.getDecoder().decode(credentialsBase64);
      return new ByteArrayInputStream(decoded);
    }

    // 2. 파일 경로가 있으면 사용
    if (credentialsPath != null && !credentialsPath.isEmpty()) {
      log.info("Loading Firebase credentials from file: {}", credentialsPath);
      return Files.newInputStream(Path.of(credentialsPath));
    }

    // 3. 기본 classpath 리소스
    log.info("Loading Firebase credentials from classpath");
    ClassPathResource resource =
        new ClassPathResource("configs/firebase/solicare-firebase-admin.json");
    return resource.getInputStream();
  }
}
