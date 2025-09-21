package com.solicare.app.backend.domain.entity;

import com.solicare.app.backend.application.enums.MonitorMode;
import com.solicare.app.backend.application.enums.SeniorEvent;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CareAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeniorEvent eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitorMode monitorMode;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String base64Image;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDismissed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_uuid", nullable = false)
    private Senior senior;
}
