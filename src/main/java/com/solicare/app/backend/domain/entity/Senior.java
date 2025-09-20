package com.solicare.app.backend.domain.entity;

import com.solicare.app.backend.domain.enums.Gender;

import jakarta.persistence.*;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
public class Senior {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @Column(nullable = false, length = 20, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String note;

    @Column(nullable = false)
    @Setter
    @Builder.Default
    private Boolean monitored = false;

    @Builder.Default
    @OneToMany(mappedBy = "senior", cascade = CascadeType.ALL)
    private List<Device> devices = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "senior", cascade = CascadeType.ALL)
    private List<Care> cares = new ArrayList<>();
}
