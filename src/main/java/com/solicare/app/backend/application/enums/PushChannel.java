package com.solicare.app.backend.application.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PushChannel {
    INFO("info_channel", "ic_notification_info"),
    ALERT("alert_channel", "ic_notification_alert");

    private final String channelId;
    private final String notificationIcon;
}
