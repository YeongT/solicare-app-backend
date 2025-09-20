package com.solicare.app.backend.domain.service;

import com.solicare.app.backend.domain.repository.SeniorSensorStatRepository;

import jakarta.transaction.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MonitorService {
    private final SeniorSensorStatRepository seniorSensorStatRepository;
}
