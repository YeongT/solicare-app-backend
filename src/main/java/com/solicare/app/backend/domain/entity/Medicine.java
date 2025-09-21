package com.solicare.app.backend.domain.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "senior_uuid")
    private Senior senior;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column private Double doseAmount;

    @Column(length = 20)
    private String doseInstruction;

    @Column(name = "days_of_week")
    private String daysOfWeekStr;

    @Column(name = "intake_time")
    private String intakeTimeStr;

    @Column(length = 500)
    private String memo;

    public Medicine linkSenior(Senior senior) {
        this.senior = senior;
        return this;
    }
}
