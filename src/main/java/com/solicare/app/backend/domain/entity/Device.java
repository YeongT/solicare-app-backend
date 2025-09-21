package com.solicare.app.backend.domain.entity;

import com.solicare.app.backend.domain.enums.PushMethod;
import com.solicare.app.backend.domain.enums.Role;

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

    public Role getOwnerRole() {
        if (getMember() != null) {
            return Role.MEMBER;
        } else if (getSenior() != null) {
            return Role.SENIOR;
        } else {
            return null;
        }
    }

    public String getOwnerUuid() {
        if (getMember() != null) {
            return getMember().getUuid();
        } else if (getSenior() != null) {
            return getSenior().getUuid();
        } else {
            return null;
        }
    }
}
