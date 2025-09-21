package com.solicare.app.backend.domain.entity;

import com.solicare.app.backend.domain.enums.PushMethod;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"pushMethod", "token"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    //    @Builder.Default @Column private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PushMethod pushMethod;

    @Column(nullable = false, length = 2048)
    private String token;

    @Column private LocalDateTime lastSeenAt;

    //  @Column(length = 20)
    //  private String deviceId; // 기기 고유번호
    //
    //  @Column(length = 20)
    //  private String deviceType; // ANDROID, IOS 등
    //
    //  @Column(length = 50)
    //  private String deviceModel; // 기기 모델명
    //
    //  @Column(length = 20)
    //  private String appVersion; // 앱 버전
    //
    //  @Column(length = 20)
    //  private String osVersion; // OS 버전

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_uuid")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_uuid")
    private Senior senior;

    public Device link(Member member) {
        this.member = member;
        this.senior = null;
        this.touch();
        return this;
    }

    private void touch() {
        this.lastSeenAt = LocalDateTime.now();
    }

    public Device link(Senior senior) {
        this.member = null;
        this.senior = senior;
        this.touch();
        return this;
    }

    public Device unlink() {
        this.member = null;
        this.senior = null;
        this.touch();
        return this;
    }

    public Device renew(String token) {
        this.token = token;
        this.touch();
        return this;
    }
}
