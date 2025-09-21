package com.solicare.app.backend.domain.entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SeniorSensorStat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Boolean cameraFallDetected;

    @Column(nullable = false)
    private Boolean wearableFallDetected;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Double humidity;

    @Column(nullable = false)
    private Integer heartRate;

    @Column(nullable = false)
    private Double wearableBattery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_uuid", nullable = false)
    private Senior senior;
}
