package com.solicare.app.backend.domain.service;

import com.google.firebase.messaging.*;
import com.solicare.app.backend.application.enums.PushChannel;
import com.solicare.app.backend.domain.dto.push.PushDeliveryResult;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FirebaseService {
    private final FirebaseMessaging firebaseMessaging;

    public PushDeliveryResult sendMessageTo(
            String fcmToken,
            PushChannel channel,
            String title,
            String body,
            Optional<Map<String, String>> data) {
        try {
            Message.Builder messageBuilder =
                    Message.builder()
                            .setToken(fcmToken)
                            .setNotification(
                                    Notification.builder().setTitle(title).setBody(body).build())
                            .setAndroidConfig(
                                    AndroidConfig.builder()
                                            .setNotification(
                                                    AndroidNotification.builder()
                                                            .setChannelId(channel.getChannelId())
                                                            .setIcon(channel.getNotificationIcon())
                                                            .build())
                                            .build())
                            .setWebpushConfig(
                                    WebpushConfig.builder()
                                            .putHeader("TTL", "300")
                                            .setNotification(
                                                    WebpushNotification.builder()
                                                            .setTitle(title)
                                                            .setBody(body)
                                                            .build())
                                            .build());
            data.ifPresent(messageBuilder::putAllData);
            firebaseMessaging.send(messageBuilder.build());
            return PushDeliveryResult.of(PushDeliveryResult.Status.SENT, null);
        } catch (Exception e) {
            if (e instanceof FirebaseMessagingException fcmEx) {
                return PushDeliveryResult.of(
                        PushDeliveryResult.Status.ERROR,
                        new RuntimeException(
                                "FCM Error: " + fcmEx.getErrorCode() + " - " + fcmEx.getMessage()));
            } else {
                return PushDeliveryResult.of(
                        PushDeliveryResult.Status.ERROR, new RuntimeException(e.getMessage()));
            }
        }
    }
}
