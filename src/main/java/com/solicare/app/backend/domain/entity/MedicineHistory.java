package com.solicare.app.backend.domain.entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_uuid")
    private Medicine medicine;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(nullable = false)
    private Double intakeAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    public enum Status {
        TAKEN,
        SKIPPED,
        MISSED
    }
}
