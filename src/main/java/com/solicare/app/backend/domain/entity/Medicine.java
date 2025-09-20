package com.solicare.app.backend.domain.entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "medicine_schedule_days",
            joinColumns = @JoinColumn(name = "medicine_uuid"))
    @Column(name = "day_of_week")
    private Set<DayOfWeek> daysOfWeek;

    @Column(length = 500)
    private String memo;

    private LocalTime intakeTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "medicine_time_categories",
            joinColumns = @JoinColumn(name = "medicine_uuid"))
    @Column(name = "time_category")
    private Set<String> timeCategories;

    public Medicine linkSenior(Senior senior) {
        this.senior = senior;
        return this;
    }
}
